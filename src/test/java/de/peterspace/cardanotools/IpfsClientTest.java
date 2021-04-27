package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.ipfs.IpfsClient;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
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
}
