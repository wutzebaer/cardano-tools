package de.peterspace.cardanominter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.validation.ConstraintViolationException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanominter.cli.CardanoCli;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CardanoCliTests {

	@Autowired
	CardanoCli cardanoCli;

	@Test
	void tipQuery() throws Exception {
		long tip = cardanoCli.queryTip();
		assertThat(tip).isGreaterThan(0);
	}

	@Test
	void createAccount() throws Exception {
		cardanoCli.createAccount();
	}

	@Test
	void getUtxoWithInvalidAccountKey() throws Exception {
		Exception exception = assertThrows(NoSuchElementException.class, () -> {
			cardanoCli.getUtxo("c:\\");
		});
		String expectedMessage = "No value present";
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	void getUtxoWithValidAccountKey() throws Exception {
		String key = cardanoCli.createAccount();
		JSONObject utxo = cardanoCli.getUtxo(key);
		long balance = cardanoCli.calculateBalance(utxo);
		assertEquals(0, balance);
	}

	@Test
	void getBalanceWithDepositedAccountKey() throws Exception {
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		JSONObject utxo = cardanoCli.getUtxo(key);
		long balance = cardanoCli.calculateBalance(utxo);
		assertThat(balance).isGreaterThan(1000000000 - 1);
	}

	@Test
	void mintCoin() throws Exception {
		// String key = cardanoCli.createAccount();
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		// String key = "ddd6369b-e1aa-4cba-b9c6-ee0db3df40fc";
		while (cardanoCli.calculateBalance(cardanoCli.getUtxo(key)) < 1) {
			log.info("Please uploads funds with https://developers.cardano.org/en/testnets/cardano/tools/faucet/ to {}", cardanoCli.getAddress(key));
			Thread.sleep(1000);
		}
		String receiver = "addr_test1vzs760mglmuup9kef90lt8vpd7f3uj5ne8xmm80xnljx2dcmmjkl8";
		cardanoCli.mintCoin(key, receiver, "AAAAA", 1000000, Map.of("HAAH", "HOHO"));
	}

}
