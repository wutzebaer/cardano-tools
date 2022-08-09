package de.peterspace.cardanotools.cardano;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardanoNode {

	@Getter
	private String era;

	private final CardanoCliDockerBridge cardanoCliDockerBridge;

	@PostConstruct
	public void init() throws Exception {
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
		cmd.add("query");
		cmd.add("tip");
		String jsonString = cardanoCliDockerBridge.requestCardanoCli(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject;
	}

}
