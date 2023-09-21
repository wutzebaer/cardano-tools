package de.peterspace.cardanotools.rest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import de.peterspace.cardanotools.TrackExecutionTime;
import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.rest.dto.EpochStakesRequest;
import de.peterspace.cardanotools.rest.dto.StakeRewardPosition;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class StakeRewardRestInterface {

	private static final long ONE_ADA = 1_000_000l;

	@Value("${pledge-address}")
	private String pledgeAddress;

	private final CardanoCli cardanoCli;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@TrackExecutionTime
	@PostMapping("{key}/{poolHash}/{epoch}")
	public ResponseEntity<List<StakeRewardPosition>> getEpochStakes(@PathVariable("key") UUID key, @PathVariable("poolHash") String poolHash, @PathVariable int epoch, @RequestBody EpochStakesRequest epochStakesRequest) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<List<StakeRewardPosition>>(HttpStatus.NOT_FOUND);
		}

		List<Utxo> accountUtxos = cardanoDbSyncClient.getUtxos(account.get().getAddress().getAddress());
		long lovelace = accountUtxos.stream().filter(u -> u.getMaPolicyId() == null).mapToLong(u -> u.getValue()).sum();
		List<Utxo> tokenData = accountUtxos.stream().filter(utxo -> utxo.getMaPolicyId() != null).toList();

		List<StakeRewardPosition> stakeRewardPositions = distributeFunds(epochStakesRequest.tip, tokenData, lovelace, poolHash, epoch, epochStakesRequest.minStake, epochStakesRequest.excludedStakers);

		ResponseEntity<Transaction> transaction = buildTransaction(key, stakeRewardPositions, epochStakesRequest.message);

		stakeRewardPositions = distributeFunds(epochStakesRequest.tip, tokenData, lovelace - transaction.getBody().getFee(), poolHash, epoch, epochStakesRequest.minStake, epochStakesRequest.excludedStakers);

		return new ResponseEntity<List<StakeRewardPosition>>(stakeRewardPositions, HttpStatus.OK);
	}

	private List<StakeRewardPosition> distributeFunds(boolean tip, List<Utxo> tokenData, Long availableLovelace, String poolHash, int epoch, long minStake, List<String> excludedStakers) throws Exception {

		List<EpochStake> epochStake2 = cardanoDbSyncClient.getEpochStake(poolHash, epoch);
		List<EpochStake> epochStake = epochStake2
				.stream()
				.filter(es -> es.getAmount() > minStake)
				.filter(es -> !excludedStakers.contains(es.getStakeAddress()))
				.sorted(Comparator.comparing(EpochStake::getAmount).reversed())
				.toList();

		if (tip) {
			availableLovelace -= ONE_ADA;
		}

		long totalStake = epochStake.stream().mapToLong(es -> es.getAmount()).sum();

		List<StakeRewardPosition> stakeRewardPositions = epochStake.stream().map(es -> {
			StakeRewardPosition srp = new StakeRewardPosition();
			srp.setAmount(es.getAmount());
			srp.setStakeAddress(es.getStakeAddress());
			return srp;
		}).collect(Collectors.toList());

		// calculate share
		for (StakeRewardPosition srp : stakeRewardPositions) {
			double share = (double) srp.getAmount() / totalStake;
			srp.setShare(share);
		}

		// distribute tokens
		for (StakeRewardPosition srp : stakeRewardPositions) {
			// add token share to output
			for (Utxo td : tokenData) {
				long tokenAmount = (long) (td.getValue() * srp.getShare());
				if (tokenAmount > 0) {
					String tokenKey = td.getMaPolicyId() + "." + td.getMaName();
					srp.getOutputs().put(tokenKey, tokenAmount);
				}
			}
		}

		// distribute remaining tokens
		if (!stakeRewardPositions.isEmpty()) {
			StakeRewardPosition srp = stakeRewardPositions.get(0);
			for (Utxo td : tokenData) {
				String tokenKey = td.getMaPolicyId() + "." + td.getMaName();
				long tokenLeft = td.getValue() - stakeRewardPositions.stream().mapToLong(es -> es.getOutputs().getOrDefault(tokenKey, 0l)).sum();
				if (tokenLeft > 0) {
					srp.getOutputs().put(tokenKey, srp.getOutputs().getOrDefault(tokenKey, 0l) + tokenLeft);
				}
			}
		}

		// add minutxo to output
		for (StakeRewardPosition srp : stakeRewardPositions) {
			String outputsInCLiFormat = toCliFormat("12345", srp.getOutputs());
			long minOutput = Math.max(cardanoCli.calculateMinUtxo(outputsInCLiFormat), ONE_ADA);
			srp.getOutputs().put("", minOutput);
			availableLovelace -= minOutput;
		}

		// distribute ada
		long lovelaceDistributed = 0;
		for (StakeRewardPosition srp : stakeRewardPositions) {
			long additionalLovelaces = Math.max((long) (availableLovelace * srp.getShare()), 0);
			srp.getOutputs().put("", srp.getOutputs().getOrDefault("", 0l) + additionalLovelaces);
			lovelaceDistributed += additionalLovelaces;
		}

		availableLovelace -= lovelaceDistributed;

		if (tip) {
			StakeRewardPosition srp = new StakeRewardPosition();
			srp.setAmount(0l);
			srp.setStakeAddress("stake1uxyt389wtpccs6t26f248d9qszgxmya2qc6a3k06jw2el9g42aktg");
			srp.setShare(0);
			srp.setOutputs(Map.of("", ONE_ADA + Math.max(availableLovelace, 0l)));
			stakeRewardPositions.add(srp);
		}

		return stakeRewardPositions;

	}

	private String toCliFormat(String address, Map<String, Long> outputs) {
		List<String> outputBits = outputs.entrySet().stream().map(e -> e.getValue() + " " + e.getKey()).collect(Collectors.toList());
		outputBits.add(0, address);
		return outputBits.stream().collect(Collectors.joining("+"));
	}

	@PostMapping("{key}/buildTransaction")
	public ResponseEntity<Transaction> buildTransaction(@PathVariable("key") UUID key, @RequestBody List<StakeRewardPosition> stakeRewardPositions, @RequestParam String message) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs();
		stakeRewardPositions.parallelStream().forEach(srp -> {
			String returnAddress = cardanoDbSyncClient.getReturnAddress(srp.getStakeAddress());
			for (Entry<String, Long> currencyEntry : srp.getOutputs().entrySet()) {
				// add output
				transactionOutputs.add(returnAddress, currencyEntry.getKey(), currencyEntry.getValue());
			}
		});

		String messageJson = null;
		if (!StringUtils.isBlank(message)) {
			messageJson = new JSONObject().put("674", new JSONObject().put("msg", new JSONArray().put(message))).toString();
		}

		Transaction transaction = cardanoCli.buildTransaction(account.get().getAddress(), transactionOutputs, messageJson);

		transaction.setMinOutput(stakeRewardPositions.stream().mapToLong(es -> es.getOutputs().getOrDefault("", 0l)).sum());

		return new ResponseEntity<Transaction>(transaction, HttpStatus.OK);
	}

}
