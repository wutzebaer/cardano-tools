package de.peterspace.cardanotools.rest;

import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
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
import de.peterspace.cardanotools.repository.MintCoinOrderRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsCli ipfsCli;
	private final MintCoinOrderRepository mintCoinOrderRepository;
	private final AccountRepository accountRepository;

	@GetMapping("tip")
	public long tip() throws Exception {
		return cardanoCli.queryTip();
	}

	@PostMapping("createAccount")
	public String createAccount() throws Exception {
		return cardanoCli.createAccount();
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

	@PostMapping(path = "addFile/{key}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String handleFileUpload(@PathVariable("key") UUID key, @RequestPart MultipartFile file) throws Exception {
		String stageFile = ipfsCli.stageFile(file.getInputStream());
		return stageFile;
	}

	@PostMapping("mintFee/{key}")
	public long mintFee(@PathVariable("key") UUID key, @RequestBody MintOrder parameterObject) throws Exception {
		return cardanoCli.calculateTransactionFee(parameterObject);
	}

	@PostMapping("mintCoinOrder/{key}")
	public void postMintCoinOrder(@PathVariable("key") UUID key, @RequestBody MintOrder mintCoinOrder) throws Exception {
		mintCoinOrderRepository.save(mintCoinOrder);
	}

	@GetMapping("mintCoinOrder/{key}")
	public ResponseEntity<MintOrder> getMintCoinOrder(@PathVariable("key") UUID key) throws Exception {
		Optional<MintOrder> mintCoinOrderOptional = mintCoinOrderRepository.findById(key.toString());
		if (mintCoinOrderOptional.isPresent()) {
			return new ResponseEntity<MintOrder>(mintCoinOrderOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<MintOrder>(HttpStatus.NOT_FOUND);
		}
	}

}
