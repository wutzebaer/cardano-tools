package de.peterspace.cardanotools.rest;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONStringer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mint")
public class MintRestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsClient ipfsClient;
	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@PostMapping(path = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String postFile(@RequestPart MultipartFile file) throws Exception {
		String ipfsData = ipfsClient.addFile(file.getInputStream());
		return JSONStringer.valueToString(ipfsData);
	}

	@PostMapping("buildMintTransaction/{key}")
	public ResponseEntity<Transaction> buildMintTransaction(@PathVariable("key") UUID key, @Valid @RequestBody MintOrderSubmission mintOrderSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		List<Utxo> accountUtxos = cardanoDbSyncClient.getUtxos(account.get().getAddress().getAddress());
		List<String> sourceAddresses = accountUtxos.stream().map(u -> u.getSourceAddress()).toList();

		if (!accountUtxos.isEmpty()
				&& !StringUtils.isBlank(mintOrderSubmission.getTargetAddress())
				&& !sourceAddresses.contains(mintOrderSubmission.getTargetAddress())) {
			throw new Exception("Invalid target address.");
		}

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrderSubmission, account.get());
		return new ResponseEntity<Transaction>(mintTransaction, HttpStatus.OK);
	}

	@PostMapping("submitMintTransaction/{key}")
	public ResponseEntity<Void> submitMintTransaction(@PathVariable("key") UUID key, @RequestBody Transaction mintTransaction) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		mintTransaction.setAccount(account.get());
		mintTransaction.setSubmitDate(new Date());
		cardanoCli.submitTransaction(mintTransaction);
		mintTransactionRepository.save(mintTransaction);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}
