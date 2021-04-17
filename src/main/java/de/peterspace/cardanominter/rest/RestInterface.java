package de.peterspace.cardanominter.rest;

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

import de.peterspace.cardanominter.cli.CardanoCli;
import de.peterspace.cardanominter.ipfs.IpfsCli;
import de.peterspace.cardanominter.model.MintCoinOrder;
import de.peterspace.cardanominter.repository.MintCoinOrderRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsCli ipfsCli;
	private final MintCoinOrderRepository mintCoinOrderRepository;

	@GetMapping("tip")
	public long tip() throws Exception {
		return cardanoCli.queryTip();
	}

	@PostMapping("createAccount")
	public String createAccount() throws Exception {
		return cardanoCli.createAccount();
	}

	@GetMapping("getBalance/{key}")
	public long getBalance(@PathVariable("key") UUID key) throws Exception {
		JSONObject utxo = cardanoCli.getUtxo(key.toString());
		return cardanoCli.calculateBalance(utxo);
	}

	@GetMapping("getAddress/{key}")
	public String getAddress(@PathVariable("key") UUID key) throws Exception {
		return cardanoCli.getAddress(key.toString());
	}

	@PostMapping(path = "addFile/{key}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String handleFileUpload(@PathVariable("key") UUID key, @RequestPart MultipartFile file) throws Exception {
		String stageFile = ipfsCli.stageFile(file.getInputStream());
		return stageFile;
	}

	@PostMapping("mintFee/{key}")
	public long mintFee(@PathVariable("key") UUID key, @RequestBody MintCoinOrder parameterObject) throws Exception {
		return cardanoCli.mintFee(key.toString(), parameterObject.getReceiver(), parameterObject.getTokenName(), parameterObject.getTokenAmount(), parameterObject.getMetaData());
	}

	@PostMapping("mintCoinOrder/{key}")
	public void postMintCoinOrder(@PathVariable("key") UUID key, @RequestBody MintCoinOrder mintCoinOrder) throws Exception {
		mintCoinOrderRepository.save(mintCoinOrder);
		// cardanoCli.mintCoin(key.toString(), parameterObject.getReceiver(),
		// parameterObject.getTokenName(), parameterObject.getTokenAmount(),
		// parameterObject.getMetaData());
	}

	@GetMapping("mintCoinOrder/{key}")
	public ResponseEntity<MintCoinOrder> getMintCoinOrder(@PathVariable("key") UUID key) throws Exception {
		Optional<MintCoinOrder> mintCoinOrderOptional = mintCoinOrderRepository.findById(key.toString());
		if (mintCoinOrderOptional.isPresent()) {
			return new ResponseEntity<MintCoinOrder>(mintCoinOrderOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<MintCoinOrder>(HttpStatus.NOT_FOUND);
		}
	}

}
