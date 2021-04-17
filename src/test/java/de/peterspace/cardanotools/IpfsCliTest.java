package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.ipfs.IpfsCli;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class IpfsCliTest {

	@Autowired
	IpfsCli ipfsCli;

	@Test
	void testSaveAndLoadFile() throws Exception {

		String data = UUID.randomUUID().toString();

		ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
		String stageName = ipfsCli.stageFile(is);
		String ipfsHash = ipfsCli.saveFile(stageName);
		assertThat(ipfsHash).hasSize(46);

		InputStream is1 = ipfsCli.getFile(ipfsHash);
		String loaded = IOUtils.toString(is1, "UTF-8");

		assertEquals(data, loaded);

	}
}
