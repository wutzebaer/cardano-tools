package de.peterspace.cardanominter.rest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.http.MediaType;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;
	private final IpfsCli ipfsCli;

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

	@PostMapping("mintCoin/{key}")
	public void mintCoin(@PathVariable("key") UUID key, @RequestBody MintCoinTask parameterObject) throws Exception {
		cardanoCli.mintCoin(key.toString(), parameterObject.getReceiver(), parameterObject.getTokenName(), parameterObject.getTokenAmount(), parameterObject.getMetaData());
	}

	@PostMapping("mintFee/{key}")
	public long mintFee(@PathVariable("key") UUID key, @RequestBody MintCoinTask parameterObject) throws Exception {
		return cardanoCli.mintFee(key.toString(), parameterObject.getReceiver(), parameterObject.getTokenName(), parameterObject.getTokenAmount(), parameterObject.getMetaData());
	}

	@PostMapping(path = "addFile/{key}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String handleFileUpload(@PathVariable("key") UUID key, @RequestPart MultipartFile file) throws Exception {
		String stageFile = ipfsCli.stageFile(file.getInputStream());
		return stageFile;
	}
}
