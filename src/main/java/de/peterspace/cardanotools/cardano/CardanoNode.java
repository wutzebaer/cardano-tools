package de.peterspace.cardanotools.cardano;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanotools.process.ProcessUtil;
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
	@Value("${cardano-node.container-name}")
	private String containerName;

	@Getter
	@Value("${cardano-node.ipc-volume-name}")
	private String ipcVolumeName;

	@PostConstruct
	public void init() throws Exception {

		// determine network
		Date systemStart;
		if (network.equals("testnet")) {
			networkMagicArgs = new String[] { "--testnet-magic", "1097911063" };
			systemStart = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse("2019-07-24T20:20:16Z")));
		} else if (network.equals("mainnet")) {
			networkMagicArgs = new String[] { "--mainnet" };
			systemStart = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse("2017-09-23T21:44:51Z")));
		} else {
			throw new RuntimeException("Network must be testnet or mainnet");
		}

		// determine container name
		if (StringUtils.isBlank(containerName)) {
			this.containerName = network + "-node";
		}

		// determine ipc volume name
		if (StringUtils.isBlank(ipcVolumeName)) {
			this.ipcVolumeName = network + "-ipc";
		}

		String runningContainers = ProcessUtil.runCommand(new String[] { "docker", "ps" });

		// ensure running node
		if (runningContainers.contains(containerName)) {
			log.info("Container {} already running.", containerName);
		} else {
			startContainer();
		}

		// ensure node is synced
		long epochLength = 432000;
		while (true) {
			try {
				JSONObject tip = queryTip();
				if (tip.get("epoch") == JSONObject.NULL) {
					log.error("Not synced: {}", tip.toString());
				} else {
					long elapsedSeconds = (System.currentTimeMillis() - systemStart.getTime()) / 1000;
					long expectedEpoch = elapsedSeconds / epochLength;
					log.info("Synced {}", String.format("%.2f%%", (double) tip.getLong("epoch") / expectedEpoch * 100));
					if (tip.getLong("epoch") == expectedEpoch) {
						break;
					}
				}
			} catch (Exception e) {
				log.error("Not synced: {}", e.getMessage());
			}
			Thread.sleep(10000);
		}
	}

	private JSONObject queryTip() throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add("docker");
		cmd.add("exec");

		cmd.add("-e");
		cmd.add("CARDANO_NODE_SOCKET_PATH=/ipc/node.socket");

		cmd.add(containerName);

		cmd.add("cardano-cli");
		cmd.add("query");
		cmd.add("tip");

		cmd.addAll(List.of(networkMagicArgs));
		String jsonString = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject;
	}

	private void startContainer() throws Exception {
		ProcessUtil.runCommand(new String[] { "docker", "pull", "inputoutput/cardano-node" });

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");
		cmd.add("run");

		cmd.add("--name");
		cmd.add(containerName);

		cmd.add("--rm");
		cmd.add("-d");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-v");
		cmd.add(network + "-data:/data");

		cmd.add("-v");
		cmd.add(ipcVolumeName + ":/ipc");

		cmd.add("-e");
		cmd.add("NETWORK=" + network);

		cmd.add("inputoutput/cardano-node");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}
}
