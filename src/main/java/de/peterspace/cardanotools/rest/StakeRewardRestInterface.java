package de.peterspace.cardanotools.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
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

	private final CardanoCli cardanoCli;
	private final IpfsClient ipfsClient;
	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("{poolHash}/{epoch}")
	public ResponseEntity<List<EpochStakePosition>> getEpochStakes(@PathVariable("poolHash") String poolHash, @PathVariable int epoch) throws Exception {
		List<EpochStakePosition> epochStake = cardanoDbSyncClient.epochStake(poolHash, epoch);
		return new ResponseEntity<List<EpochStakePosition>>(epochStake, HttpStatus.OK);
	}

	@PostMapping("{key}/buildTransaction")
	public ResponseEntity<Transaction> buildTransaction(@PathVariable("key") UUID key, @RequestBody StakeRewardSubmission stakeRewardSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs(stakeRewardSubmission.getOutputs());

		Transaction transaction = cardanoCli.buildTransaction(account.get().getAddress(), transactionOutputs);
		return new ResponseEntity<Transaction>(transaction, HttpStatus.OK);
	}

}
