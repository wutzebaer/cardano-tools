package de.peterspace.cardanominter.cli;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanominter.annotations.AnnotationHelper;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
public class CardanoCli {

	private String[] cardanoCliCmd;
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

	public long queryTip() throws Exception {
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

	public JSONObject getUtxo(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key) throws Exception {

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

		return readUtxo;
	}

	public long calculateBalance(JSONObject utxo) throws Exception {
		long sum = 0;

		Iterator<String> txidIterator = utxo.keys();
		while (txidIterator.hasNext()) {
			String txid = txidIterator.next();
			sum += utxo.getJSONObject(txid).getJSONObject("value").getLong("lovelace");
		}

		return sum;
	}

	public void mintCoin(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key, String receiver, String tokenName, long tokenAmount) throws Exception {
		JSONObject utxo = getUtxo(key);
		long tip = queryTip();
		createMintTransaction(key, utxo, receiver, tokenName, tokenAmount, 0, tip);
		long fee = calculateFee(key, utxo);
		createMintTransaction(key, utxo, receiver, tokenName, tokenAmount, fee, tip);
		signTransaction(key);
	}

	private void createMintTransaction(String key, JSONObject utxo, String receiver, String tokenName, long tokenAmount, long fee, long tip) throws Exception {
		long balance = calculateBalance(utxo);
		long dueSlot = createPolicy(key, tip);
		String policyId = getPolicyId(key);

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));

		cmd.add("transaction");
		cmd.add("build-raw");

		cmd.add("--fee");
		cmd.add("" + fee);

		Iterator<String> utxoKeys = utxo.keys();
		while (utxoKeys.hasNext()) {
			cmd.add("--tx-in");
			cmd.add(utxoKeys.next());
		}

		cmd.add("--tx-out");
		cmd.add(String.format("%s+%d+%d %s.%s", receiver, balance - fee, tokenAmount, policyId, tokenName));

		cmd.add("--mint");
		cmd.add(String.format("%d %s.%s", tokenAmount, policyId, tokenName));

		cmd.add("--out-file");
		cmd.add(key + ".raw");

		cmd.add("--invalid-hereafter");
		cmd.add("" + dueSlot);

		runCommand(cmd.toArray(new String[0]));
	}

	private long calculateFee(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key, JSONObject utxo) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));

		cmd.add("transaction");
		cmd.add("calculate-min-fee");

		cmd.add("--tx-body-file");
		cmd.add(key + ".raw");

		cmd.add("--tx-in-count");
		int count = 0;
		Iterator<String> keysIterator = utxo.keys();
		while (keysIterator.hasNext()) {
			keysIterator.next();
			count++;
		}
		cmd.add("" + count);

		cmd.add("--tx-out-count");
		cmd.add("1");

		cmd.add("--witness-count");
		cmd.add("1");

		cmd.addAll(List.of(networkMagicCmd));

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		String fee = runCommand(cmd.toArray(new String[0]));
		return Long.parseLong(fee.split(" ")[0]);
	}

	private void signTransaction(String key) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("sign");

		cmd.add("--signing-key-file");
		cmd.add(key + ".skey");

		cmd.add("--script-file");
		cmd.add(key + ".script");

		cmd.addAll(List.of(networkMagicCmd));

		cmd.add("--tx-body-file");
		cmd.add(key + ".raw");

		cmd.add("--out-file");
		cmd.add(key + ".signed");

		runCommand(cmd.toArray(new String[0]));
	}

	private long createPolicy(String key, long tip) throws Exception {

		// slot
		long dueSlot = tip + 60 * 10;

		// address hash
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-hash");
		cmd.add("--payment-verification-key-file");
		cmd.add(key + ".vkey");
		String keyHash = runCommand(cmd.toArray(new String[0]));

		// @formatter:off
        JSONObject script = new JSONObject()
                .put("type", "all")
                .put("scripts",
                        new JSONArray()
                                .put(new JSONObject()
                                        .put("slot", dueSlot)
                                        .put("type", "before"))
                                .put(new JSONObject()
                                        .put("keyHash", keyHash)
                                        .put("type", "sig")));
        // @formatter:on
		writeFile(key + ".script", script.toString(3));

		return dueSlot;

	}

	private String getPolicyId(@Pattern(regexp = AnnotationHelper.UUID_PATTERN, message = "TokenFormatError") String key) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("policyid");
		cmd.add("--script-file");
		cmd.add(key + ".script");
		String policyId = runCommand(cmd.toArray(new String[0]));
		return policyId;
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
		return Files.readString(Paths.get(workingDir, filename));
	}

	private void writeFile(String filename, String content) throws Exception {
		Files.writeString(Paths.get(workingDir, filename), content);
	}

	private String runCommand(String... cmd) throws Exception {
		log.trace(StringUtils.join(Arrays.copyOfRange(cmd, cardanoCliCmd.length, cmd.length), " "));
		Process process = Runtime.getRuntime().exec(cmd);
		int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new Exception(new String(process.getErrorStream().readAllBytes()).trim());
		} else {
			return new String(process.getInputStream().readAllBytes()).trim();
		}
	}

}
