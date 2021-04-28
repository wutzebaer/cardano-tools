package de.peterspace.cardanotools.cardano;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.ChangeAction;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.MintTransaction;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.process.ProcessUtil;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardanoCli {

	@Value("${pledge-address}")
	private String pledgeAddress;
	private final CardanoNode cardanoNode;
	private final AccountRepository accountRepository;

	@Value("${working.dir}")
	private String workingDir;

	private String[] cardanoCliCmd;
	private String[] networkMagicArgs;
	private Account dummyAccount;

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

		dummyAccount = createAccount();

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

	public MintTransaction buildMintTransaction(MintOrderSubmission mintOrderSubmission, Account account) throws Exception {
		JSONObject utxo = getUtxo(account);

		// fake if account is not funded
		if (utxo.length() == 0) {
			utxo = new JSONObject()
					.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAccount.getAddress()).put("value", new JSONObject().put("lovelace", 1000000000l)));
		}
		if (mintOrderSubmission.getTargetAddress() == null) {
			mintOrderSubmission.setTargetAddress(dummyAccount.getAddress());
		}

		writeFile(account.getKey() + ".vkey", account.getVkey());
		writeFile(account.getKey() + ".skey", account.getSkey());
		MintTransaction mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, 0);

		long fee = calculateFee(mintTransaction, utxo);
		long neededBalance = mintTransaction.getMinOutput() + fee + (mintOrderSubmission.getTip() ? 1000000 : 0);
		if (!utxo.has("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0") && account.getBalance() < neededBalance) {
			// simulate a further input, because the user has to make another utxo
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAccount.getAddress()).put("value", new JSONObject().put("lovelace", 1000000000l)));
			mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, 0);
			fee = calculateFee(mintTransaction, utxo);
		}

		mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, fee);
		removeFile(account.getKey() + ".vkey");
		removeFile(account.getKey() + ".skey");
		return mintTransaction;
	}

	public void executeMintTransaction(MintTransaction mintTransaction) throws Exception {
		Account account = mintTransaction.getAccount();

		writeFile(account.getKey() + ".vkey", account.getVkey());
		writeFile(account.getKey() + ".skey", account.getSkey());

		signTransaction(mintTransaction, account);
		submitTransaction(mintTransaction);

		removeFile(account.getKey() + ".vkey");
		removeFile(account.getKey() + ".skey");
	}

	private MintTransaction createMintTransaction(MintOrderSubmission mintOrderSubmission, Account account, JSONObject utxo, long fee) throws Exception {

		String temporaryFilePrefix = UUID.randomUUID().toString();

		long tip = queryTip();
		long balance = calculateBalance(utxo);
		JSONObject policyScript = createPolicy(mintOrderSubmission, account, tip);
		String policyId = getPolicyId(policyScript);

		String metadataFilename = temporaryFilePrefix + ".metadata";
		JSONObject metadata = new JSONObject();
		JSONObject policyMetadata = new JSONObject();
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			JSONObject cleanedMetadata = token.getCleanedMetadata();
			if (cleanedMetadata != null) {
				policyMetadata.put(token.getAssetName(), cleanedMetadata);
			}
		}
		if (policyMetadata.length() > 0) {
			metadata.put(policyId, policyMetadata);
		}
		metadata.put("scripts", new JSONObject().put(policyId, policyScript));
		String metadataJson = new JSONObject().put("721", metadata).toString(3);
		writeFile(metadataFilename, metadataJson);

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

		long minOutput = MinOutputCalculator.calculate(mintOrderSubmission.getTokens());
		TransactionOutputs transactionOutputs = new TransactionOutputs();

		// add ada change and new minted coins
		if (mintOrderSubmission.getTip()) {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), "", minOutput);
		} else {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), "", balance - fee);
		}
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), policyId + "." + token.getAssetName(), token.getAmount());
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
						transactionOutputs.add(mintOrderSubmission.getTargetAddress(), otherPolicyId + "." + otherPolicyToken, amount);
					}
				}
			}
		}

		if (mintOrderSubmission.getTip()) {
			long change = balance - minOutput - fee;
			transactionOutputs.add(pledgeAddress, "", Math.max(change, 0));
		}

		for (String a : transactionOutputs.toCliFormat()) {
			cmd.add("--tx-out");
			cmd.add(a);
		}

		cmd.add("--mint");
		List<String> mints = new ArrayList<String>();
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			mints.add(String.format("%d %s.%s", token.getAmount(), policyId, token.getAssetName()));
		}
		cmd.add(StringUtils.join(mints, "+"));

		cmd.add("--json-metadata-no-schema");
		cmd.add("--metadata-json-file");
		cmd.add(metadataFilename);

		cmd.add("--out-file");
		cmd.add(temporaryFilePrefix + ".raw");

		cmd.add("--invalid-hereafter");
		cmd.add("" + policyScript.getJSONArray("scripts").getJSONObject(0).getLong("slot"));

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		MintTransaction mintTransaction = new MintTransaction();
		mintTransaction.setFee(fee);
		mintTransaction.setInputs(utxo.toString(3));
		mintTransaction.setOutputs(new JSONObject(transactionOutputs.getOutputs()).toString(3));
		mintTransaction.setPolicy(policyScript.toString(3));
		mintTransaction.setPolicyId(policyId);
		mintTransaction.setMetaDataJson(metadataJson);
		mintTransaction.setRawData(readFile(temporaryFilePrefix + ".raw"));
		mintTransaction.setMintOrderSubmission(mintOrderSubmission);
		mintTransaction.setMinOutput(minOutput);

		String txId = getTxId(mintTransaction);
		mintTransaction.setTxId(txId);

		removeFile(temporaryFilePrefix + ".metadata");
		removeFile(temporaryFilePrefix + ".raw");

		return mintTransaction;
	}

	private long calculateFee(MintTransaction mintTransaction, JSONObject utxo) throws Exception {
		String filename = UUID.randomUUID().toString() + ".raw";
		writeFile(filename, mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));

		cmd.add("transaction");
		cmd.add("calculate-min-fee");

		cmd.add("--tx-body-file");
		cmd.add(filename);

		cmd.add("--tx-in-count");
		int count = 0;
		Iterator<String> keysIterator = utxo.keys();
		while (keysIterator.hasNext()) {
			keysIterator.next();
			count++;
		}
		cmd.add("" + count);

		cmd.add("--tx-out-count");
		cmd.add(new JSONObject(mintTransaction.getOutputs()).length() + "");

		cmd.add("--witness-count");
		cmd.add("1");

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		String feeString = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		long fee = Long.valueOf(feeString.split(" ")[0]);

		removeFile(filename);

		return fee;
	}

	public long calculateMinOutputSize(MintTransaction mintTransaction) throws Exception {
		long minUTxOValue = 1000000;
		return 0;
	}

	private void signTransaction(MintTransaction mintTransaction, Account account) throws Exception {

		String filePrefix = UUID.randomUUID().toString();

		writeFile(filePrefix + ".script", mintTransaction.getPolicy());
		writeFile(filePrefix + ".raw", mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("sign");

		cmd.add("--signing-key-file");
		cmd.add(account.getKey() + ".skey");

		cmd.add("--script-file");
		cmd.add(filePrefix + ".script");

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--tx-body-file");
		cmd.add(filePrefix + ".raw");

		cmd.add("--out-file");
		cmd.add(filePrefix + ".signed");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		mintTransaction.setSignedData(consumeFile(filePrefix + ".signed"));
		removeFile(filePrefix + ".script");
		removeFile(filePrefix + ".raw");
	}

	private void submitTransaction(MintTransaction mintTransaction) throws Exception {
		String filename = UUID.randomUUID().toString() + ".signed";
		try {

			writeFile(filename, mintTransaction.getSignedData());

			ArrayList<String> cmd = new ArrayList<String>();
			cmd.addAll(List.of(cardanoCliCmd));
			cmd.add("transaction");
			cmd.add("submit");

			cmd.add("--tx-file");
			cmd.add(filename);

			cmd.addAll(List.of(networkMagicArgs));

			ProcessUtil.runCommand(cmd.toArray(new String[0]));

		} catch (Exception e) {
			if (e.getMessage().contains("BadInputsUTxO")) {
				throw new Exception("You have unprocessed transactions, please wait a minute.");
			} else if (e.getMessage().contains("OutsideValidityIntervalUTxO")) {
				throw new Exception("You policy has expired. Confirm to generate a new one.");
			} else {
				throw e;
			}
		} finally {
			removeFile(filename);
		}
	}

	private JSONObject createPolicy(MintOrderSubmission mintOrderSubmission, Account account, long tip) throws Exception {

		// slot
		long dueSlot = tip + 60 * 10;

		// address hash
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-hash");
		cmd.add("--payment-verification-key-file");
		cmd.add(account.getKey() + ".vkey");
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

		return script;

	}

	private String getPolicyId(JSONObject policy) throws Exception {
		String filename = UUID.randomUUID().toString() + ".script";
		writeFile(filename, policy.toString(3));

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("policyid");
		cmd.add("--script-file");
		cmd.add(filename);
		String policyId = ProcessUtil.runCommand(cmd.toArray(new String[0]));

		removeFile(filename);

		return policyId;
	}

	private String getTxId(MintTransaction mintTransaction) throws Exception {

		String filename = UUID.randomUUID().toString() + ".raw";
		writeFile(filename, mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("txid");
		cmd.add("--tx-body-file");
		cmd.add(filename);
		String txId = ProcessUtil.runCommand(cmd.toArray(new String[0]));

		removeFile(filename);

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
