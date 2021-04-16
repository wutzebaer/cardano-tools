package de.peterspace.cardanominter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.peterspace.cardanominter.model.Address;
import de.peterspace.cardanominter.repository.AddressRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class RepositoryTest {

	@Autowired
	AddressRepository addressRepository;

	@Test
	void testSaveAndLoadFile() throws Exception {
		String id = UUID.randomUUID().toString();

		Address address = new Address(id, "sdfdsfsd345324");
		addressRepository.save(address);

		Optional<Address> loadedAddress = addressRepository.findById(id);
		assertTrue(loadedAddress.isPresent());
		assertEquals(loadedAddress.get().getAddress(), "sdfdsfsd345324");
	}

}
