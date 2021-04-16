package de.peterspace.cardanominter.ipfs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanominter.cli.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IpfsCli {

	@Value("${working.dir}")
	private String workingDir;

	public String saveFile(InputStream is) throws Exception {
		File tempFile = new File(workingDir + "/ipfs/export/" + UUID.randomUUID().toString());
		try (OutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
			IOUtils.copy(is, bos);
		}

		String result = ProcessUtil.runCommand(new String[] { "docker", "exec", "ipfs-node", "ipfs", "add", "-r", "/export/" + tempFile.getName() });
		return result.split(" ")[1];
	}

}
