package de.peterspace.cardanominter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanominter.ipfs.IpfsCli;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class IpfsCliTest {

	@Autowired
	IpfsCli ipfsCli;

	@Test
	void testSaveFile() throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
		String ipfsHash = ipfsCli.saveFile(is);
		assertThat(ipfsHash).hasSize(46);
	}
}
