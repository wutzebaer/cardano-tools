package de.peterspace.cardanominter.rest;

import java.util.UUID;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanominter.cli.CardanoCli;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final CardanoCli cardanoCli;

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
}
