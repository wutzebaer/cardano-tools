package de.peterspace.cardanotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.repository.AccountRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RepositoryTest {

	@Autowired
	AccountRepository addressRepository;

	@Autowired
	CardanoCli cardanoCli;

	@Test
	void testSaveAndLoadFile() throws Exception {
		Account account = cardanoCli.createAccount();
		addressRepository.save(account);

		Optional<Account> loadedAddress = addressRepository.findById(account.getKey());
		assertTrue(loadedAddress.isPresent());
		assertEquals(loadedAddress.get().getAddress().getAddress(), account.getAddress().getAddress());
	}

}
