package de.peterspace.cardanotools.cardano;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CardanoCliDockerBridge {

	@Value("${network}")
	private String network;

	@Value("${cardano-node.ipc-volume-name}")
	private String ipcVolumeName;

	@Value("${cardano-node.version}")
	private String nodeVersion;

	@Value("${working.dir}")
	private String workingDir;

	@Value("${cardano-cli-docker-bridge.url}")
	private String bridgeUrl;

	@Getter
	private String[] networkMagicArgs;

	@lombok.Value
	public static class CardanoCliDockerBridgeRequest {
		String ipcVolumeName;
		String workingDir;
		String nodeVersion;
		String[] networkMagicArgs;
		String[] cmd;
	}

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
	}

	public String requestCardanoCliNomagic(String[] cmd) throws Exception {
		return request(cmd, new String[] {}, "cardano-cli");
	}

	public String requestCardanoCli(String[] cmd) throws Exception {
		return request(cmd, networkMagicArgs, "cardano-cli");
	}

	public String requestMetadataCreator(String[] cmd) throws Exception {
		return request(cmd, new String[] {}, "cardano-tools-token-metadata-creator");
	}

	private String request(String[] cmd, String[] networkMagicArgs, String path) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		CardanoCliDockerBridgeRequest cardanoCliDockerBridgeRequest = new CardanoCliDockerBridgeRequest(ipcVolumeName, workingDir, nodeVersion, networkMagicArgs, cmd);
		log.info("Running docker " + new JSONArray(cmd).toString(3));
		ResponseEntity<String> response = restTemplate.postForEntity(bridgeUrl + "/" + path, cardanoCliDockerBridgeRequest, String.class);
		log.info("Result " + response.getBody());
		if (response.getStatusCodeValue() == 200) {
			return response.getBody();
		} else {
			throw new Exception(response.getBody());
		}
	}

}
