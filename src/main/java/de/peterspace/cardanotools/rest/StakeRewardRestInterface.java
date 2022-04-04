package de.peterspace.cardanotools.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import de.peterspace.cardanotools.model.StakeRewardSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class StakeRewardRestInterface {

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

		List<EpochStakePosition> epochStake = cardanoDbSyncClient.epochStake(poolHash, epoch);
		epochStake.removeIf(es -> es.getAmount() < minStake);

		List<TokenData> tokenData = new ObjectMapper().readValue(account.get().getAddress().getTokensData(), new TypeReference<List<TokenData>>() {
		});
		Long lovelace = account.get().getAddress().getBalance();

		if (tip) {
			lovelace -= 1_000_000l;
		}

		long totalStake = epochStake.stream().mapToLong(es -> es.getAmount()).sum();

		// distribute ada
		for (EpochStakePosition es : epochStake) {
			es.getOutputs().put("", Math.min(lovelace * (es.getAmount() / totalStake), 1_000_000l));
			for (TokenData td : tokenData) {
				es.getOutputs().put(td.getPolicyId() + "." + td.getName(), td.getQuantity() * (es.getAmount() / totalStake));
			}
			Set<String> assetNames = es.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[1]).collect(Collectors.toSet());
			if (!assetNames.isEmpty()) {
				int policies = es.getOutputs().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[0]).collect(Collectors.toSet()).size();
				long minOutput = MinOutputCalculator.calculate(assetNames, policies);
				if (es.getOutputs().get("") < minOutput) {
					es.getOutputs().put("", minOutput);
				}
			}
		}

		if (tip) {
			epochStake.add(new EpochStakePosition(0, "cardano-tools.io", pledgeAddress, Map.of("", 1_000_000l)));
		}

		return new ResponseEntity<List<EpochStakePosition>>(epochStake, HttpStatus.OK);
	}

	@PostMapping("{key}/buildTransaction")
	public ResponseEntity<Transaction> buildTransaction(@PathVariable("key") UUID key, @RequestBody StakeRewardSubmission stakeRewardSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs();
		for (Entry<String, Map<String, Long>> targetEntry : stakeRewardSubmission.getOutputs().entrySet()) {
			for (Entry<String, Long> currencyEntry : targetEntry.getValue().entrySet()) {
				// add output
				if (StringUtils.isEmpty(currencyEntry.getKey())) {
					transactionOutputs.add(targetEntry.getKey(), "", currencyEntry.getValue());
				} else {
					String[] bits = currencyEntry.getKey().split("\\.");
					transactionOutputs.add(targetEntry.getKey(), bits[0] + "." + Hex.encodeHexString(bits[1].getBytes(StandardCharsets.UTF_8)), currencyEntry.getValue());
				}
			}

			// check min output
			Set<String> assetNames = targetEntry.getValue().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[1]).collect(Collectors.toSet());
			if (!assetNames.isEmpty()) {
				int policies = targetEntry.getValue().keySet().stream().filter(k -> !StringUtils.isEmpty(k)).map(k -> k.split("\\.")[0]).collect(Collectors.toSet()).size();
				long minOutput = MinOutputCalculator.calculate(assetNames, policies);
				if (targetEntry.getValue().get("") < minOutput) {
					transactionOutputs.add(targetEntry.getKey(), "", minOutput - targetEntry.getValue().get(""));
				}
			}
		}

		if (stakeRewardSubmission.getTip()) {
			transactionOutputs.add(pledgeAddress, "", 1_000_000);
		}

		Transaction transaction = cardanoCli.buildTransaction(account.get().getAddress(), transactionOutputs);
		return new ResponseEntity<Transaction>(transaction, HttpStatus.OK);
	}

}
