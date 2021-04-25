package de.peterspace.cardanotools.cardano;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrder;
import de.peterspace.cardanotools.model.Token;
import de.peterspace.cardanotools.process.ProcessUtil;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintOrderRepository;
import de.peterspace.cardanotools.rest.dto.ChangeAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardanoCli {

	private final long minOutput = 1000000l;
	private final CardanoNode cardanoNode;
	private final AccountRepository accountRepository;
	private final MintOrderRepository mintOrderRepository;

	@Value("${working.dir}")
	private String workingDir;

	private String[] cardanoCliCmd;
	private String[] networkMagicArgs;

	@PostConstruct
	public void init() throws Exception {

		// @formatter:off
        cardanoCliCmd = new String[] {
                "docker", "exec",
                "-w", "/workdir",
                "-e", "CARDANO_NODE_SOCKET_PATH=/ipc/node.socket",
                cardanoNode.getContainerName(),
                "cardano-cli"
        };
        // @formatter:on
		this.networkMagicArgs = cardanoNode.getNetworkMagicArgs();

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("protocol-parameters");
		cmd.add("--out-file");
		cmd.add("protocol.json");
		cmd.addAll(List.of(networkMagicArgs));
		ProcessUtil.runCommand(cmd.toArray(new String[0]));

	}

	public long queryTip() throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("tip");
		cmd.addAll(List.of(networkMagicArgs));
		String jsonString = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject.getLong("slot");
	}

	public Account createAccount() throws Exception {

		String key = UUID.randomUUID().toString();

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-gen");
		cmd.add("--verification-key-file");
		cmd.add(key + ".vkey");
		cmd.add("--signing-key-file");
		cmd.add(key + ".skey");
		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(key + ".vkey");
		cmd.addAll(List.of(networkMagicArgs));
		String addressLiteral = ProcessUtil.runCommand(cmd.toArray(new String[0]));

		Account account = new Account(key, new Date(), addressLiteral, readFile(key + ".skey"), readFile(key + ".vkey"), new ArrayList<>(), 0l, 0l);
		accountRepository.save(account);

		removeFile(key + ".skey");
		removeFile(key + ".vkey");

		return account;
	}

	public JSONObject getUtxo(Account account) throws Exception {

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("utxo");
		cmd.add("--address");
		cmd.add(account.getAddress());
		cmd.addAll(List.of(networkMagicArgs));
		cmd.add("--out-file");
		cmd.add(account.getKey() + ".utxo");
		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		String readString = consumeFile(account.getKey() + ".utxo");
		JSONObject readUtxo = new JSONObject(readString);

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

	public List<String> collectTransactionHashes(JSONObject utxo) throws Exception {
		List<String> txids = new ArrayList<>();

		Iterator<String> txidIterator = utxo.keys();
		while (txidIterator.hasNext()) {
			String txid = txidIterator.next();
			txids.add(txid.split("#")[0]);
		}

		return txids;
	}

	public long calculateTransactionFee(MintOrder mintOrder) throws Exception {
		JSONObject utxo = getUtxo(mintOrder.getAccount());
		writeFile(mintOrder.getAccount().getKey() + ".vkey", mintOrder.getAccount().getVkey());
		writeFile(mintOrder.getAccount().getKey() + ".skey", mintOrder.getAccount().getSkey());
		long tip = queryTip();
		createMintTransaction(mintOrder, utxo, 0, tip);
		long fee = calculateFee(mintOrder.createFilePrefix(), utxo);
		if (mintOrder.getAccount() != null) {
			removeFile(mintOrder.getAccount().getKey() + ".vkey");
			removeFile(mintOrder.getAccount().getKey() + ".skey");
		}
		return fee;
	}

	public void executeMintOrder(MintOrder mintOrder) throws Exception {
		writeFile(mintOrder.getAccount().getKey() + ".vkey", mintOrder.getAccount().getVkey());
		writeFile(mintOrder.getAccount().getKey() + ".skey", mintOrder.getAccount().getSkey());

		JSONObject utxo = getUtxo(mintOrder.getAccount());
		long tip = queryTip();
		createMintTransaction(mintOrder, utxo, 0, tip);
		long fee = calculateFee(mintOrder.createFilePrefix(), utxo);
		createMintTransaction(mintOrder, utxo, fee, tip);
		signTransaction(mintOrder);
		submitTransaction(mintOrder.createFilePrefix());

		mintOrder.setPolicyScript(readFile(mintOrder.createFilePrefix() + ".script"));
		mintOrder.setTxid(getTxId(mintOrder));
		mintOrderRepository.save(mintOrder);

		removeFile(mintOrder.getAccount().getKey() + ".vkey");
		removeFile(mintOrder.getAccount().getKey() + ".skey");
		removeFile(mintOrder.createFilePrefix() + ".script");
		removeFile(mintOrder.createFilePrefix() + ".raw");
		removeFile(mintOrder.createFilePrefix() + ".signed");
		removeFile(mintOrder.createFilePrefix() + ".metadata");
	}

	private void createMintTransaction(MintOrder mintOrder, JSONObject utxo, long fee, long tip) throws Exception {
		long balance = calculateBalance(utxo);
		long dueSlot = createPolicy(mintOrder, tip);
		String policyId = getPolicyId(mintOrder);

		String metadataFilename = mintOrder.createFilePrefix() + ".metadata";
		JSONObject metadata = new JSONObject();
		JSONObject policyMetadata = new JSONObject();
		for (Token token : mintOrder.getTokens()) {
			if (token.getMetaDataJson() != null) {
				policyMetadata.put(token.getAssetName(), new JSONObject(token.getMetaDataJson()));
			}
		}
		metadata.put(policyId, policyMetadata);
		writeFile(metadataFilename, new JSONObject().put("721", metadata).toString(3));

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

		Map<String, Map<String, Long>> outputs = new HashMap<>();

		cmd.add("--tx-out");
		// add ada change and new minted coins
		List<String> outputs = new ArrayList<String>();
		outputs.add(mintOrder.getTargetAddress());
		if (mintOrder.getChangeAction() != ChangeAction.RETURN) {
			outputs.add("" + minOutput);
		} else {
			outputs.add("" + (balance - fee));
		}
		for (Token token : mintOrder.getTokens()) {
			outputs.add(String.format("%d %s.%s", token.getAmount(), policyId, token.getAssetName()));
		}
		// the account might have other minted tokens, which also has to be sent
		utxoKeys = utxo.keys();
		while (utxoKeys.hasNext()) {
			String txid = utxoKeys.next();
			JSONObject value = utxo.getJSONObject(txid).getJSONObject("value");
			Iterator<String> valueKeys = value.keys();
			while (valueKeys.hasNext()) {
				String otherPolicyId = valueKeys.next();
				if (!otherPolicyId.equals("lovelace")) {
					JSONObject otherPolicy = value.getJSONObject(otherPolicyId);
					Iterator<String> otherPolicyTokens = otherPolicy.keys();
					while (otherPolicyTokens.hasNext()) {
						String otherPolicyToken = otherPolicyTokens.next();
						long amount = otherPolicy.getLong(otherPolicyToken);
						outputs.add(String.format("%d %s.%s", amount, otherPolicyId, otherPolicyToken));
					}
				}
			}
		}
		// combine all outputs
		cmd.add(StringUtils.join(outputs, "+"));

		if (mintOrder.getChangeAction() != ChangeAction.RETURN) {
			long change = balance - minOutput - fee;
			cmd.add("--tx-out");
			if (mintOrder.getChangeAction() == ChangeAction.KEEP) {
				cmd.add(mintOrder.getAccount().getAddress() + "+" + change);
			} else if (mintOrder.getChangeAction() == ChangeAction.TIP) {
				cmd.add(mintOrder.getAccount().getAddress() + "+" + change);
			}
		}

		cmd.add("--mint");
		List<String> mints = new ArrayList<String>();
		for (Token token : mintOrder.getTokens()) {
			mints.add(String.format("%d %s.%s", token.getAmount(), policyId, token.getAssetName()));
		}
		cmd.add(StringUtils.join(mints, "+"));

		cmd.add("--json-metadata-no-schema");

		cmd.add("--metadata-json-file");
		cmd.add(metadataFilename);

		cmd.add("--out-file");
		cmd.add(mintOrder.createFilePrefix() + ".raw");

		cmd.add("--invalid-hereafter");
		cmd.add("" + dueSlot);

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}

	private long calculateFee(String key, JSONObject utxo) throws Exception {
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

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		String fee = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		return Long.parseLong(fee.split(" ")[0]);
	}

	private void signTransaction(MintOrder mintOrder) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("sign");

		cmd.add("--signing-key-file");
		cmd.add(mintOrder.getAccount().getKey() + ".skey");

		cmd.add("--script-file");
		cmd.add(mintOrder.createFilePrefix() + ".script");

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--tx-body-file");
		cmd.add(mintOrder.createFilePrefix() + ".raw");

		cmd.add("--out-file");
		cmd.add(mintOrder.createFilePrefix() + ".signed");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}

	private void submitTransaction(String key) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("submit");

		cmd.add("--tx-file");
		cmd.add(key + ".signed");

		cmd.addAll(List.of(networkMagicArgs));

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}

	private long createPolicy(MintOrder mintOrder, long tip) throws Exception {

		// slot
		long dueSlot = tip + 60 * 10;

		// address hash
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-hash");
		cmd.add("--payment-verification-key-file");
		cmd.add(mintOrder.getAccount().getKey() + ".vkey");
		String keyHash = ProcessUtil.runCommand(cmd.toArray(new String[0]));

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
		writeFile(mintOrder.createFilePrefix() + ".script", script.toString(3));

		return dueSlot;

	}

	private String getPolicyId(MintOrder mintOrder) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("policyid");
		cmd.add("--script-file");
		cmd.add(mintOrder.createFilePrefix() + ".script");
		String policyId = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		return policyId;
	}

	private String getTxId(MintOrder mintOrder) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("txid");
		cmd.add("--tx-body-file");
		cmd.add(mintOrder.createFilePrefix() + ".raw");
		String txId = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		return txId;
	}

	private String consumeFile(String filename) throws Exception {
		Path path = Paths.get(workingDir, filename);
		String readString = Files.readString(path);
		Files.delete(path);
		return readString;
	}

	private String readFile(String filename) throws Exception {
		return Files.readString(Paths.get(workingDir, filename));
	}

	private void writeFile(String filename, String content) throws Exception {
		Files.writeString(Paths.get(workingDir, filename), content);
	}

	private void removeFile(String filename) throws Exception {
		Files.delete(Paths.get(workingDir, filename));
	}

}
