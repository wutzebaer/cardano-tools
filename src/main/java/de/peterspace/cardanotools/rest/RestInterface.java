package de.peterspace.cardanotools.rest;

import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.MintTransaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import de.peterspace.cardanotools.rest.dto.TransferAccount;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsClient ipfsClient;
	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("tip")
	public long getTip() throws Exception {
		return cardanoCli.queryTip();
	}

	@PostMapping("createAccount")
	public TransferAccount createAccount() throws Exception {
		Account account = cardanoCli.createAccount();
		return TransferAccount.from(account);
	}

	@GetMapping("account/{key}")
	public ResponseEntity<TransferAccount> getAccount(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();

			JSONObject utxo = cardanoCli.getUtxo(account);
			account.setBalance(cardanoCli.calculateBalance(utxo));
			account.setFundingAddresses(cardanoDbSyncClient.getFundingAddresses(account.getAddress()));
			account.setLastUpdate(System.currentTimeMillis());
			accountRepository.save(account);

			return new ResponseEntity<TransferAccount>(TransferAccount.from(accountOptional.get()), HttpStatus.OK);
		} else {
			return new ResponseEntity<TransferAccount>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(path = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String postFile(@RequestPart MultipartFile file) throws Exception {
		String ipfsData = ipfsClient.addFile(file.getInputStream());
		return JSONStringer.valueToString(ipfsData);
	}

	@PostMapping("buildMintTransaction/{key}")
	public ResponseEntity<MintTransaction> buildMintTransaction(@PathVariable("key") UUID key, @RequestBody MintOrderSubmission mintOrderSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<MintTransaction>(HttpStatus.NOT_FOUND);
		}

		if (account.get().getBalance() > 0 && !account.get().getFundingAddresses().contains(mintOrderSubmission.getTargetAddress())) {
			throw new Exception("Invalid target address.");
		}

		MintTransaction mintTransaction = cardanoCli.buildMintTransaction(mintOrderSubmission, account.get());
		return new ResponseEntity<MintTransaction>(mintTransaction, HttpStatus.OK);
	}

	@PostMapping("submitMintTransaction/{key}")
	public ResponseEntity<Void> submitMintTransaction(@PathVariable("key") UUID key, @RequestBody MintTransaction mintTransaction) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		mintTransaction.setAccount(account.get());
		cardanoCli.executeMintTransaction(mintTransaction);
		mintTransactionRepository.save(mintTransaction);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}
