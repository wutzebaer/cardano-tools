package de.peterspace.cardanotools.rest;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.MinOutputCalculator;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.TokenData;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.EpochStakePosition;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class StakeRewardRestInterface {

	private static final long ONE_ADA = 1_000_000l;

	@Value("${pledge-address}")
	private String pledgeAddress;

	private final CardanoCli cardanoCli;
	private final IpfsClient ipfsClient;
	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("{key}/{poolHash}/{epoch}")
	public ResponseEntity<List<EpochStakePosition>> getEpochStakes(@PathVariable("key") UUID key, @PathVariable("poolHash") String poolHash, @PathVariable int epoch, @RequestParam boolean tip, @RequestParam long minStake) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<List<EpochStakePosition>>(HttpStatus.NOT_FOUND);
		}

		List<TokenData> tokenData = new ObjectMapper().readValue(account.get().getAddress().getTokensData(), new TypeReference<List<TokenData>>() {
		});
		Long lovelace = account.get().getAddress().getBalance();

		List<EpochStakePosition> epochStake = distributeFunds(tip, tokenData, lovelace, poolHash, epoch, minStake);

		ResponseEntity<Transaction> transaction = buildTransaction(key, epochStake);

		epochStake = distributeFunds(tip, tokenData, lovelace - transaction.getBody().getFee(), poolHash, epoch, minStake);

		return new ResponseEntity<List<EpochStakePosition>>(epochStake, HttpStatus.OK);
	}

	private List<EpochStakePosition> distributeFunds(boolean tip, List<TokenData> tokenData, Long lovelace, String poolHash, int epoch, long minStake) throws DecoderException {
		List<EpochStakePosition> epochStake = cardanoDbSyncClient.epochStake(poolHash, epoch).stream().sorted(Comparator.comparing(EpochStakePosition::getAmount).reversed()).collect(Collectors.toList());
		epochStake.removeIf(es -> es.getAmount() < minStake);
		epochStake.removeIf(es -> StringUtils.isBlank(es.getAddress()));

		if (tip) {
			lovelace -= ONE_ADA;
		}

		long totalStake = epochStake.stream().mapToLong(es -> es.getAmount()).sum();

		// distribute tokens
		for (EpochStakePosition es : epochStake) {
			double share = (double) es.getAmount() / totalStake;
			es.getOutputs().clear();

			for (TokenData td : tokenData) {
				long tokenAmount = (long) (td.getQuantity() * share);
				if (tokenAmount > 0) {
					String tokenKey = td.getPolicyId() + "." + td.getName();
					es.getOutputs().put(tokenKey, tokenAmount);
				}
			}

			// ensure min outputs
			Set<String> assetNames = es.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[1]).collect(Collectors.toSet());
			if (!assetNames.isEmpty()) {
				int policies = es.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[0]).collect(Collectors.toSet()).size();
				long minOutput = MinOutputCalculator.calculate(assetNames, policies);
				es.getOutputs().put("", minOutput);
				lovelace -= minOutput;
			} else {
				es.getOutputs().put("", ONE_ADA);
				lovelace -= ONE_ADA;
			}
		}

		// distribute remaining tokens
		if (!epochStake.isEmpty()) {
			EpochStakePosition epochStakePosition = epochStake.get(0);

			for (TokenData td : tokenData) {
				String tokenKey = td.getPolicyId() + "." + td.getName();
				long tokenLeft = td.getQuantity() - epochStake.stream().mapToLong(es -> es.getOutputs().getOrDefault(tokenKey, 0l)).sum();
				if (tokenLeft > 0) {
					epochStakePosition.getOutputs().put(tokenKey, epochStakePosition.getOutputs().getOrDefault(tokenKey, 0l) + tokenLeft);
				}
			}

			Set<String> assetNames = epochStakePosition.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[1]).collect(Collectors.toSet());
			if (!assetNames.isEmpty()) {
				int policies = epochStakePosition.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[0]).collect(Collectors.toSet()).size();
				long minOutput = MinOutputCalculator.calculate(assetNames, policies);
				Long currentOutput = epochStakePosition.getOutputs().getOrDefault("", 0l);
				long missingOutput = Math.max(minOutput - currentOutput, 0l);
				epochStakePosition.getOutputs().put("", currentOutput + missingOutput);
				lovelace -= missingOutput;
			}
		}

		// distribute ada
		long lovelaceDistributed = 0;
		for (EpochStakePosition es : epochStake) {
			double share = (double) es.getAmount() / totalStake;
			long additionalLovelaces = Math.max((long) (lovelace * share), 0);
			es.getOutputs().put("", es.getOutputs().getOrDefault("", 0l) + additionalLovelaces);
			lovelaceDistributed += additionalLovelaces;
		}

		lovelace -= lovelaceDistributed;

		if (tip) {
			epochStake.add(new EpochStakePosition(0, "cardano-tools.io", pledgeAddress, Map.of("", ONE_ADA + Math.max(lovelace, 0l))));
		}

		return epochStake;

	}

	@PostMapping("{key}/buildTransaction")
	public ResponseEntity<Transaction> buildTransaction(@PathVariable("key") UUID key, @RequestBody List<EpochStakePosition> epochStakePositions) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs();
		for (EpochStakePosition epochStakePosition : epochStakePositions) {
			for (Entry<String, Long> currencyEntry : epochStakePosition.getOutputs().entrySet()) {
				// add output
				if (StringUtils.isEmpty(currencyEntry.getKey())) {
					transactionOutputs.add(epochStakePosition.getAddress(), "", currencyEntry.getValue());
				} else {
					String[] bits = currencyEntry.getKey().split("\\.");
					transactionOutputs.add(epochStakePosition.getAddress(), bits[0] + "." + Hex.encodeHexString(bits[1].getBytes(StandardCharsets.UTF_8)), currencyEntry.getValue());
				}
			}
		}

		Transaction transaction = cardanoCli.buildTransaction(account.get().getAddress(), transactionOutputs);

		transaction.setMinOutput(epochStakePositions.stream().mapToLong(es -> es.getOutputs().getOrDefault("", 0l)).sum());

		return new ResponseEntity<Transaction>(transaction, HttpStatus.OK);
	}

}
