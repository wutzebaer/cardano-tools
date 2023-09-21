package de.peterspace.cardanotools.cardano;

import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import de.peterspace.cardanotools.config.TrackExecutionTime;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.ToString;
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

	@Value("${cardano-cli-docker-bridge.url}")
	private String bridgeUrl;

	@Getter
	private String[] networkMagicArgs;

	@lombok.Value
	@ToString(exclude = "inputFiles")
	public static class CardanoCliDockerBridgeRequest {
		String ipcVolumeName;
		String nodeVersion;
		String[] networkMagicArgs;
		String[] cmd;
		String[] outputFiles;
		Map<String, String> inputFiles;
	}

	@PostConstruct
	public void init() throws Exception {
		// determine network
		if (network.equals("preview")) {
			networkMagicArgs = new String[] { "--testnet-magic", "2" };
		} else if (network.equals("mainnet")) {
			networkMagicArgs = new String[] { "--mainnet" };
		} else {
			throw new RuntimeException("Network must be preview or mainnet");
		}
	}

	@TrackExecutionTime
	public String[] requestCardanoCliNomagic(Map<String, String> inputFiles, String[] cmd, String... outputFiles) throws Exception {
		return request("cardano-cli", new String[] {}, inputFiles, cmd, outputFiles);
	}

	@TrackExecutionTime
	public String[] requestCardanoCli(Map<String, String> inputFiles, String[] cmd, String... outputFiles) throws Exception {
		return request("cardano-cli", networkMagicArgs, inputFiles, cmd, outputFiles);
	}

	@TrackExecutionTime
	public String[] requestMetadataCreator(Map<String, String> inputFiles, String[] cmd, String... outputFiles) throws Exception {
		return request("cardano-tools-token-metadata-creator", new String[0], inputFiles, cmd, outputFiles);
	}

	private String[] request(String path, String[] networkMagicArgs, Map<String, String> inputFiles, String[] cmd, String[] outputFiles) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		CardanoCliDockerBridgeRequest cardanoCliDockerBridgeRequest = new CardanoCliDockerBridgeRequest(ipcVolumeName, nodeVersion, networkMagicArgs, cmd, outputFiles, inputFiles);
		log.info("Running docker " + cardanoCliDockerBridgeRequest);
		ResponseEntity<String[]> response = restTemplate.postForEntity(bridgeUrl + "/" + path, cardanoCliDockerBridgeRequest, String[].class);
		log.info("Result " + new JSONArray(response.getBody()).toString(3));
		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		} else {
			throw new Exception(response.getBody()[0]);
		}
	}

}
