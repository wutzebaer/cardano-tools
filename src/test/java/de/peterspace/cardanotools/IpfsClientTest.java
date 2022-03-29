package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.ipfs.IpfsClient;

@SpringBootTest
public class IpfsClientTest {

	@Autowired
	IpfsClient ipfsClient;

	@Test
	void testSaveAndLoadFile() throws Exception {
		String data = UUID.randomUUID().toString();
		ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
		String ipfsHash = ipfsClient.addFile(is);
		assertThat(ipfsHash).hasSize(46);
	}

	@Test
	void testPin() throws Exception {
		String ipfsUrl = "ipfs://QmT1fJsGrLpZ3sYSsvCdPyQYTrg3mGFX2h1fjKfHvnFp2m";
		ipfsClient.pinFile(ipfsUrl);
	}

	@Test
	void testSize() throws Exception {
		String ipfsUrl = "ipfs://QmV5SkozRyQJD9facdup9fKqynAu4omjmLm4iTKiM5nSM1";
		Integer size = ipfsClient.getSize(ipfsUrl);
		assertThat(size).isEqualTo(9513);
	}

}
