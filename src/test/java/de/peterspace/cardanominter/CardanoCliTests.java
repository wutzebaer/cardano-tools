package de.peterspace.cardanominter;

import static org.assertj.core.api.Assertions.assertThat;

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
}
