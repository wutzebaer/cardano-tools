package de.peterspace.cardanominter.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanominter.annotations.AnnotationHelper;

@Component
@Validated
public class CardanoCli {

	// cardano.cli.path=docker run --rm -v testnet-ipc:/ipc -v
	// C:\\github\\cardano-minter\\cardano-minter-workdir:/workdir -w /workdir -e
	// CARDANO_NODE_SOCKET_PATH=/ipc/node.socket --entrypoint cardano-cli
	// inputoutput/cardano-node
	// cat.path=docker run --rm -v
	// C:\\github\\cardano-minter\\cardano-minter-workdir:/workdir -w /workdir
	// --entrypoint cat inputoutput/cardano-node

	private String[] cardanoCliCmd;
	private String[] catCmd;
	private String[] networkMagicCmd;

	@Value("${cardano.node.socket.volume}")
	private String cardanoNodeSocketVolume;

	@Value("${cardano.node.socket.path}")
	private String cardanoNodeSocketPath;

	@Value("${working.dir}")
	private String workingDir;

	@Value("${network.magic}")
	private String networkMagic;

	@PostConstruct
	public void init() throws Exception {

		// @formatter:off
		cardanoCliCmd = new String[] {
			"docker", "run", "--rm",
			"-v", cardanoNodeSocketVolume + ":/ipc",
			"-v", workingDir + ":/workdir",
			"-w", "/workdir",
			"-e", "CARDANO_NODE_SOCKET_PATH=" + cardanoNodeSocketPath,
			"--entrypoint", "cardano-cli",
			"inputoutput/cardano-node"
		};
		catCmd = new String[] {
			"docker", "run", "--rm",
			"-v", workingDir + ":/workdir",
			"-w", "/workdir",
			"--entrypoint", "cat",
			"inputoutput/cardano-node"
		};
		networkMagicCmd = networkMagic.split(" ");
		// @formatter:on

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("protocol-parameters");
		cmd.add("--out-file");
		cmd.add("protocol.json");
		cmd.addAll(List.of(networkMagicCmd));
		runCommand(cmd.toArray(new String[0]));
	}

	public long getTip() throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("tip");
		cmd.addAll(List.of(networkMagicCmd));
		String jsonString = runCommand(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject.getLong("slot");
	}

	public String createAccount() throws Exception {

		String key = UUID.randomUUID().toString();

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-gen");
		cmd.add("--verification-key-file");
		cmd.add(key + ".vkey");
		cmd.add("--signing-key-file");
		cmd.add(key + ".skey");
		runCommand(cmd.toArray(new String[0]));

		cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(key + ".vkey");
		cmd.add("--out-file");
		cmd.add(key + ".addr");
		cmd.addAll(List.of(networkMagicCmd));
		runCommand(cmd.toArray(new String[0]));

		return key;
	}

	public long getBalance(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key) throws Exception {

		String address = readAddress(key);

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("utxo");
		cmd.add("--address");
		cmd.add(address);
		cmd.addAll(List.of(networkMagicCmd));
		cmd.add("--out-file");
		cmd.add(key + ".utxo");
		runCommand(cmd.toArray(new String[0]));

		JSONObject readUtxo = readUtxo(key);

		long sum = 0;

		Iterator<String> txidIterator = readUtxo.keys();
		while (txidIterator.hasNext()) {
			String txid = txidIterator.next();
			sum += readUtxo.getJSONObject(txid).getJSONObject("value").getLong("lovelace");
		}

		return sum;
	}

	private String readAddress(String key) throws Exception {
		return readFile(key + ".addr");
	}

	private JSONObject readUtxo(String key) throws Exception {
		String readString = readFile(key + ".utxo");
		JSONObject jsonObject = new JSONObject(readString);
		return jsonObject;
	}

	private String readFile(String filename) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(catCmd));
		cmd.add(filename);
		return runCommand(cmd.toArray(new String[0]));
	}

	private String runCommand(String... cmd) throws Exception {
		Process process = Runtime.getRuntime().exec(cmd);
		int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new Exception(new String(process.getErrorStream().readAllBytes()).trim());
		} else {
			return new String(process.getInputStream().readAllBytes()).trim();
		}
	}

}
