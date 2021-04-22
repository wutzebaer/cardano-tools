package de.peterspace.cardanotools.ipfs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanotools.process.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IpfsCli {

	@Value("${working.dir}")
	private String workingDir;

	public String stageFile(InputStream is) throws Exception {
		File stageFile = new File(workingDir + "/ipfs/export/" + UUID.randomUUID().toString());
		try (OutputStream bos = new BufferedOutputStream(new FileOutputStream(stageFile))) {
			IOUtils.copy(is, bos);
		}
		return stageFile.getName();
	}

	public String saveFile(String stageFile, boolean pin) throws Exception {
		String result = ProcessUtil.runCommand(new String[] { "docker", "exec", "ipfs-node", "ipfs", "add", "--pin=" + pin, "/export/" + stageFile });
		new File(workingDir + "/ipfs/export/" + stageFile).delete();
		return result.split(" ")[1];
	}

	public InputStream getFile(String ipfsHash) throws Exception {
		File file = new File(workingDir + "/ipfs/export/" + ipfsHash);
		if (!file.isFile()) {
			ProcessUtil.runCommand(new String[] { "docker", "exec", "ipfs-node", "ipfs", "get", ipfsHash, "-o", "/export/" + ipfsHash });
		}
		return new FileInputStream(file);
	}

}
