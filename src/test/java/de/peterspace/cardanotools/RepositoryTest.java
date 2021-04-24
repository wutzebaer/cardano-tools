package de.peterspace.cardanotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class RepositoryTest {

	@Autowired
	AccountRepository addressRepository;

	@Test
	void testSaveAndLoadFile() throws Exception {
		String id = UUID.randomUUID().toString();

		Account address = new Account(id, new Date(), "sdfdsfsd345324", "a", "b", new ArrayList<>(), 0l, 0l);
		addressRepository.save(address);

		Optional<Account> loadedAddress = addressRepository.findById(id);
		assertTrue(loadedAddress.isPresent());
		assertEquals(loadedAddress.get().getAddress(), "sdfdsfsd345324");
	}

}
