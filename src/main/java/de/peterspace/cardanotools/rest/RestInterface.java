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
import de.peterspace.cardanotools.ipfs.IpfsCli;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrder;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintOrderRepository;
import de.peterspace.cardanotools.rest.dto.MintOrderSubmission;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsCli ipfsCli;
	private final MintOrderRepository mintOrderRepository;
	private final AccountRepository accountRepository;

	@GetMapping("tip")
	public long tip() throws Exception {
		return cardanoCli.queryTip();
	}

	@PostMapping("createAccount")
	public String createAccount() throws Exception {
		return JSONStringer.valueToString(cardanoCli.createAccount().getKey());
	}

	@GetMapping("getBalance/{key}")
	public ResponseEntity<Long> getBalance(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (account.isPresent()) {
			JSONObject utxo = cardanoCli.getUtxo(account.get());
			long blanace = cardanoCli.calculateBalance(utxo);
			return new ResponseEntity<Long>(blanace, HttpStatus.OK);
		} else {
			return new ResponseEntity<Long>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("getAddress/{key}")
	public ResponseEntity<String> getAddress(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (account.isPresent()) {
			return new ResponseEntity<String>(account.get().getAddress(), HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(path = "addFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String addFile(@RequestPart MultipartFile file) throws Exception {
		String stageFile = ipfsCli.stageFile(file.getInputStream());
		return JSONStringer.valueToString(ipfsCli.saveFile(stageFile, true));
	}

	@PostMapping("mintFee")
	public ResponseEntity<Long> calculateFee(@RequestBody MintOrderSubmission mintOrderSubmission) throws Exception {
		MintOrder mintOrder = mintOrderSubmission.toMintOrder(null);
		long fee = cardanoCli.calculateTransactionFee(mintOrder);
		return new ResponseEntity<Long>(fee, HttpStatus.OK);
	}

	@PostMapping("mintCoinOrder/{key}")
	public ResponseEntity<Void> postMintCoinOrder(@PathVariable("key") UUID key, @RequestBody MintOrderSubmission mintOrderSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		MintOrder mintOrder = mintOrderSubmission.toMintOrder(account.get());
		mintOrderRepository.save(mintOrder);

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@GetMapping("mintCoinOrder/{key}")
	public ResponseEntity<MintOrder> getMintCoinOrder(@PathVariable("key") UUID key) throws Exception {
		Optional<MintOrder> mintCoinOrderOptional = mintOrderRepository.findById(key.toString());
		if (mintCoinOrderOptional.isPresent()) {
			return new ResponseEntity<MintOrder>(mintCoinOrderOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<MintOrder>(HttpStatus.NOT_FOUND);
		}
	}

}
