package de.peterspace.cardanominter.rest;

import java.util.UUID;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
