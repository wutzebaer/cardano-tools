package de.peterspace.cardanominter.ipfs;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanominter.cli.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IpfsNode {

	@Value("${working.dir}")
	private String workingDir;

	@PostConstruct
	public void init() throws Exception {

		String runningContainers = ProcessUtil.runCommand(new String[] { "docker", "ps" });

		// ensure running node
		if (runningContainers.contains("ipfs-node")) {
			log.info("Container {} already running.", "ipfs-node");
		} else {
			startContainer();
		}

	}

	private void startContainer() throws Exception {
		ProcessUtil.runCommand(new String[] { "docker", "pull", "ipfs/go-ipfs" });

		// docker run -d --name ipfs_host -e IPFS_PROFILE=server -v
		// $ipfs_staging:/export -v $ipfs_data:/data/ipfs -p 4001:4001 -p
		// 127.0.0.1:8080:8080 -p 127.0.0.1:5001:5001 ipfs/go-ipfs:latest

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");
		cmd.add("run");

		cmd.add("-d");
		cmd.add("--rm");

		cmd.add("--name");
		cmd.add("ipfs-node");

		cmd.add("-e");
		cmd.add("IPFS_PROFILE=server");

		cmd.add("-v");
		cmd.add(workingDir + "/ipfs/export:/export");

		cmd.add("-v");
		cmd.add(workingDir + "/ipfs/data:/data/ipfs");

		cmd.add("-p");
		cmd.add("4001:4001");

		cmd.add("-p");
		cmd.add("127.0.0.1:8081:8080");

		cmd.add("-p");
		cmd.add("127.0.0.1:5001:5001");

		cmd.add("ipfs/go-ipfs");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

	}
}
