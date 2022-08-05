package de.peterspace.cardanotools.cardano;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

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

	@Value("${cardano-node.ipc-volume-name}")
	private String ipcVolumeName;

	@Value("${cardano-node.version}")
	private String nodeVersion;

	@Getter
	private String[] networkMagicArgs;

	@Getter
	private String donationAddress;

	@Getter
	private String era;

	@PostConstruct
	public void init() throws Exception {

		// determine network
		if (network.equals("testnet")) {
			networkMagicArgs = new String[] { "--testnet-magic", "1097911063" };
			donationAddress = "addr_test1vzm609cpns8n6cnlpdslp4wyeym7ke3422nrt76esjwggfcpmns48";
		} else if (network.equals("mainnet")) {
			networkMagicArgs = new String[] { "--mainnet" };
			donationAddress = "addr1qx6pnsm9n3lrvtwx24kq7a0mfwq2txum2tvtaevnpkn4mpyghzw2ukr33p5k45j42w62pqysdkf65p34mrvl4yu4n72s7yfgkq";
		} else {
			throw new RuntimeException("Network must be testnet or mainnet");
		}

		// ensure node is synced
		while (true) {
			try {
				JSONObject tip = queryTip();
				double syncProgress = tip.getDouble("syncProgress");
				log.info("Synced {}", syncProgress);
				if (syncProgress == 100) {
					era = tip.getString("era");
					break;
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
		cmd.add("run");

		cmd.add("--rm");

		cmd.add("--entrypoint");
		cmd.add("cardano-cli");

		cmd.add("-v");
		cmd.add(ipcVolumeName + ":/ipc");

		cmd.add("-e");
		cmd.add("CARDANO_NODE_SOCKET_PATH=/ipc/node.socket");

		cmd.add("inputoutput/cardano-node:" + nodeVersion);

		cmd.add("query");
		cmd.add("tip");

		cmd.addAll(List.of(networkMagicArgs));
		String jsonString = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject;
	}

}
