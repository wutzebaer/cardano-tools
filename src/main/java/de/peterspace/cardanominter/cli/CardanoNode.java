package de.peterspace.cardanominter.cli;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CardanoNode {

	@Value("${network}")
	private String network;

	@Value("${working.dir}")
	private String workingDir;

	@Getter
	private String[] networkMagicArgs;

	@Getter
	private String containerName;

	@PostConstruct
	public void init() throws Exception {

		// determine network
		if (network.equals("testnet")) {
			networkMagicArgs = new String[] { "--testnet-magic", "1097911063" };
		} else if (network.equals("mainnet")) {
			networkMagicArgs = new String[] { "--mainnet" };
		} else {
			throw new RuntimeException("Network must be testnet or mainnet");
		}

		// determine container name
		this.containerName = network + "-node";

		String runningContainers = ProcessUtil.runCommand(new String[] { "docker", "ps" });

		// ensure running node
		if (runningContainers.contains(containerName)) {
			log.info("Container {} already running.", containerName);
		} else {
			startContainer();
		}

		// ensure node is synced

	}

	private void startContainer() throws Exception {
		ProcessUtil.runCommand(new String[] { "docker", "pull", "inputoutput/cardano-node:master" });

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");
		cmd.add("run");

		cmd.add("--name");
		cmd.add(containerName);

		cmd.add("--rm");
		cmd.add("-d");

		cmd.add("-v");
		cmd.add(workingDir + ":/workdir");

		cmd.add("-v");
		cmd.add(network + "-data:/data");

		cmd.add("-v");
		cmd.add(network + "-ipc:/ipc");

		cmd.add("-e");
		cmd.add("NETWORK=" + network);

		cmd.add("inputoutput/cardano-node:master");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}
}
