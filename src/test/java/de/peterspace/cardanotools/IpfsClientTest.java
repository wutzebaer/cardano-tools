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
		String ipfsUrl = "ipfs://QmUvLDcV1v1TVdkafwLJSt5tHV8pQaG7Yzh32DriKGUPfd";
		ipfsClient.pinFile(ipfsUrl);
	}

	@Test
	void testSize() throws Exception {
		String ipfsUrl = "ipfs://QmV5SkozRyQJD9facdup9fKqynAu4omjmLm4iTKiM5nSM1";
		long size = ipfsClient.getSize(ipfsUrl);
		assertThat(size).isEqualTo(9513);
	}

	@Test
	void testSize2() throws Exception {
		String ipfsUrl = "ipfs://QmaVd5KyBcTJim42MTYK8enu6DXkYEzCqeNHxXBAZ8bmdX";
		long size = ipfsClient.getSize(ipfsUrl);
		assertThat(size).isEqualTo(901707);
	}

	@Test
	void testSizeFolder() throws Exception {
		String ipfsUrl = "ipfs://QmWLwAgLKwELWq6aggrwKTzCaeHqDQaz5otjN6XChsBjCo";
		long size = ipfsClient.getSize(ipfsUrl);
		assertThat(size).isEqualTo(29954961);
	}

	@Test
	void testPinFolder() throws Exception {
		String ipfsUrl = "ipfs://QmWLwAgLKwELWq6aggrwKTzCaeHqDQaz5otjN6XChsBjCo";
		ipfsClient.pinFile(ipfsUrl);
	}

	@Test
	void testUnPinFolder() throws Exception {
		String ipfsUrl = "ipfs://QmWLwAgLKwELWq6aggrwKTzCaeHqDQaz5otjN6XChsBjCo";
		ipfsClient.unpinFile(ipfsUrl);
	}

	@Test
	void testisPinned() throws Exception {
		String ipfsUrl = "ipfs://QmUvLDcV1v1TVdkafwLJSt5tHV8pQaG7Yzh32DriKGUPfd";
		System.out.println("Pinned: " + ipfsClient.isPinned(ipfsUrl));
	}

}
