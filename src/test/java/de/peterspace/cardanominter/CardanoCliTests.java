package de.peterspace.cardanominter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanominter.cli.CardanoCli;

@SpringBootTest
public class CardanoCliTests {

	@Autowired
	CardanoCli cardanoCli;

	@Test
	void tipQuery() throws Exception {
		long tip = cardanoCli.getTip();
		assertThat(tip).isGreaterThan(0);
	}

	@Test
	void createAccount() throws Exception {
		cardanoCli.createAccount();
	}

	@Test
	void getBalanceWithInvalidAccountKey() throws Exception {
		Exception exception = assertThrows(ConstraintViolationException.class, () -> {
			cardanoCli.getBalance("c:\\");
		});
		String expectedMessage = "getBalance.key: TokenFormatError";
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	void getBalanceWithValidAccountKey() throws Exception {
		String key = cardanoCli.createAccount();
		long balance = cardanoCli.getBalance(key);
		assertEquals(0, balance);
	}

	@Test
	void getBalanceWithDepositedAccountKey() throws Exception {
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		String key = "e6041580-513b-4402-9039-083300f31235";
		long balance = cardanoCli.getBalance(key);
		assertThat(balance).isGreaterThan(1000000000 - 1);
	}

	@Test
	void creatMintTransactionWithDepositedAccountKey() throws Exception {
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		String key = "e6041580-513b-4402-9039-083300f31235";
		String receiver = "addr_test1vzs760mglmuup9kef90lt8vpd7f3uj5ne8xmm80xnljx2dcmmjkl8";
		cardanoCli.mintCoin(key, receiver, "HUHU", 1);
	}

}
