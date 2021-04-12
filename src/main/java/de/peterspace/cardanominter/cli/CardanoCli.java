package de.peterspace.cardanominter.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanominter.annotations.AnnotationHelper;

@Component
public class CardanoCli {

	@Value("#{'${cardano.cli.path}'.split(' ')}")
	private String[] cardanoCliPath;

	@Value("#{'${network.magic}'.split(' ')}")
	private String[] networkMagic;

	@PostConstruct
	public void init() throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliPath));
		cmd.add("query");
		cmd.add("protocol-parameters");
		cmd.add("--out-file");
		cmd.add("protocol.json");
		cmd.addAll(List.of(networkMagic));
		runCommand(cmd.toArray(new String[0]));
	}

	public long getTip() throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliPath));
		cmd.add("query");
		cmd.add("tip");
		cmd.addAll(List.of(networkMagic));
		String jsonString = runCommand(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject.getLong("slot");
	}

	public String createAccount() throws Exception {

		String key = UUID.randomUUID().toString();

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliPath));
		cmd.add("address");
		cmd.add("key-gen");
		cmd.add("--verification-key-file");
		cmd.add(key + ".vkey");
		cmd.add("--signing-key-file");
		cmd.add(key + ".skey");
		runCommand(cmd.toArray(new String[0]));

		cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliPath));
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(key + ".vkey");
		cmd.add("--out-file");
		cmd.add(key + ".addr");
		cmd.addAll(List.of(networkMagic));
		runCommand(cmd.toArray(new String[0]));

		return key;
	}
	
	public long getBalance(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key) {
		
	}

	private String runCommand(String... cmd) throws Exception {
		Process process = Runtime.getRuntime().exec(cmd);
		int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new Exception(new String(process.getErrorStream().readAllBytes()));
		} else {
			return new String(process.getInputStream().readAllBytes());
		}
	}

}
