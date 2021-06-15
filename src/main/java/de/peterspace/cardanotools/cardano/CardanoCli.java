package de.peterspace.cardanotools.cardano;

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

	@Value("${network}")
	private String network;

	@Value("${pledge-address}")
	private String pledgeAddress;
	private final CardanoNode cardanoNode;
	private final AccountRepository accountRepository;
	private final FileUtil fileUtil;

	@Value("${working.dir}")
	private String workingDir;

	private String[] cardanoCliCmd;
	private String[] networkMagicArgs;
	private String dummyAddress;

	@PostConstruct
	public void init() throws Exception {

		// @formatter:off
        cardanoCliCmd = new String[] {
                "docker", "exec",
                "-w", "/work",
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

		if (network.equals("testnet")) {
			dummyAddress = "addr_test1qpqxjsh6jx0mumxgw5nc5jeu5xy07k35v6h2zutyka72yua578p0hapx37mcflefvvwyhwtwn4kt83nkf7wqwx9tvsdsv8p9ac";
		} else if (network.equals("mainnet")) {
			dummyAddress = "addr1q9h7988xmmpz2y50rg2n9fw6jd5rq95t8q84k4q6ne403nxahea9slntm5n8f06nlsynyf4m6sa0qd05agra0qgk09nq96rqh9";
		} else {
			throw new RuntimeException("Network must be testnet or mainnet");
		}

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

		String vkey = fileUtil.readFile(key + ".vkey");
		Policy policy = createPolicy(vkey, queryTip());

		Account account = new Account(key, new Date(), addressLiteral, fileUtil.readFile(key + ".skey"), vkey, new ArrayList<>(), 0l, 0l, policy.getPolicy(), policy.getPolicyId(), policy.getPolicyDueDate());
		accountRepository.save(account);

		fileUtil.removeFile(key + ".skey");
		fileUtil.removeFile(key + ".vkey");

		return account;
	}

	public JSONObject getUtxo(Account account) throws Exception {

		String utxoFilename = filename("utxo");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("utxo");
		cmd.add("--address");
		cmd.add(account.getAddress());
		cmd.addAll(List.of(networkMagicArgs));
		cmd.add("--out-file");
		cmd.add(utxoFilename);
		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		String readString = fileUtil.consumeFile(utxoFilename);
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

	public MintTransaction buildMintTransaction(MintOrderSubmission mintOrderSubmission, Account account) throws Exception {
		JSONObject utxo = getUtxo(account);

		// fake if account is not funded
		if (utxo.length() == 0) {
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAddress).put("value", new JSONObject().put("lovelace", 1000000000l)));
		}

		if (StringUtils.isBlank(mintOrderSubmission.getTargetAddress())) {
			mintOrderSubmission.setTargetAddress(dummyAddress);
		}

		MintTransaction mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, 0);

		long fee = calculateFee(mintTransaction, utxo);
		long neededBalance = mintTransaction.getMinOutput() + fee + (mintOrderSubmission.getTip() ? 1000000 : 0);
		if (!utxo.has("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0") && account.getBalance() < neededBalance) {
			// simulate a further input, because the user has to make another utxo
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAddress).put("value", new JSONObject().put("lovelace", 1000000000l)));
			mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, 0);
			fee = calculateFee(mintTransaction, utxo);
		}

		mintTransaction = createMintTransaction(mintOrderSubmission, account, utxo, fee);
		signTransaction(mintTransaction, account);

		return mintTransaction;
	}

	public void executeMintTransaction(MintTransaction mintTransaction) throws Exception {
		submitTransaction(mintTransaction);
	}

	public Policy createPolicy(String vkey, long tip) throws Exception {

		int secondsToLive = 60 * 60 * 24;
		long dueSlot = tip + secondsToLive;

		String vkeyFilename = filename("vkey");
		fileUtil.writeFile(vkeyFilename, vkey);

		// address hash
		ArrayList<String> cmd1 = new ArrayList<String>();
		cmd1.addAll(List.of(cardanoCliCmd));
		cmd1.add("address");
		cmd1.add("key-hash");
		cmd1.add("--payment-verification-key-file");
		cmd1.add(vkeyFilename);
		String keyHash = ProcessUtil.runCommand(cmd1.toArray(new String[0]));

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

		fileUtil.removeFile(vkeyFilename);

		String policyFilename = filename("script");
		String policyString = script.toString(3);
		fileUtil.writeFile(policyFilename, policyString);
		ArrayList<String> cmd2 = new ArrayList<String>();
		cmd2.addAll(List.of(cardanoCliCmd));
		cmd2.add("transaction");
		cmd2.add("policyid");
		cmd2.add("--script-file");
		cmd2.add(policyFilename);
		String policyId = ProcessUtil.runCommand(cmd2.toArray(new String[0]));
		fileUtil.removeFile(policyFilename);

		return new Policy(policyString, policyId, new Date(System.currentTimeMillis() + secondsToLive * 1000));
	}

	private MintTransaction createMintTransaction(MintOrderSubmission mintOrderSubmission, Account account, JSONObject utxo, long fee) throws Exception {

		String metadataFilename = filename("metadata");
		String rawFilename = filename("raw");
		String scriptFilename = filename("script");

		final long balance = calculateBalance(utxo);
		final JSONObject policyScript = new JSONObject(account.getPolicy());
		final String policyId = account.getPolicyId();

		fileUtil.writeFile(scriptFilename, policyScript.toString(3));

		JSONObject metadata = new JSONObject();
		JSONObject policyMetadata = new JSONObject();
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			JSONObject cleanedMetadata = new JSONObject(token.getMetaData());
			cleanedMetadata.put("policy", policyScript);
			if (cleanedMetadata != null) {
				policyMetadata.put(token.getAssetName(), cleanedMetadata);
			}
		}
		if (policyMetadata.length() > 0) {
			metadata.put(policyId, policyMetadata);
		}
		String metadataJson = new JSONObject().put("721", metadata).toString(3);
		fileUtil.writeFile(metadataFilename, metadataJson);

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

		cmd.add("--minting-script-file");
		cmd.add(scriptFilename);

		cmd.add("--json-metadata-no-schema");
		cmd.add("--metadata-json-file");
		cmd.add(metadataFilename);

		cmd.add("--out-file");
		cmd.add(rawFilename);

		cmd.add("--invalid-hereafter");
		cmd.add("" + policyScript.getJSONArray("scripts").getJSONObject(0).getLong("slot"));

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		MintTransaction mintTransaction = new MintTransaction();
		mintTransaction.setFee(fee);
		mintTransaction.setInputs(utxo.toString(3));
		mintTransaction.setOutputs(new JSONObject(transactionOutputs.getOutputs()).toString(3));
		mintTransaction.setPolicy(account.getPolicy());
		mintTransaction.setPolicyId(policyId);
		mintTransaction.setMetaDataJson(metadataJson);
		mintTransaction.setRawData(fileUtil.readFile(rawFilename));
		mintTransaction.setMintOrderSubmission(mintOrderSubmission);
		mintTransaction.setMinOutput(minOutput);

		String txId = getTxId(mintTransaction);
		mintTransaction.setTxId(txId);

		fileUtil.removeFile(metadataFilename);
		fileUtil.removeFile(rawFilename);
		fileUtil.removeFile(scriptFilename);

		return mintTransaction;
	}

	private long calculateFee(MintTransaction mintTransaction, JSONObject utxo) throws Exception {
		String filename = filename("raw");
		fileUtil.writeFile(filename, mintTransaction.getRawData());

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

		fileUtil.removeFile(filename);

		return fee;
	}

	private void signTransaction(MintTransaction mintTransaction, Account account) throws Exception {

		String skeyFilename = filename("skey");
		String rawFilename = filename("raw");
		String signedFilename = filename("signed");

		fileUtil.writeFile(skeyFilename, account.getSkey());
		fileUtil.writeFile(rawFilename, mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("sign");

		cmd.add("--signing-key-file");
		cmd.add(skeyFilename);

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--tx-body-file");
		cmd.add(rawFilename);

		cmd.add("--out-file");
		cmd.add(signedFilename);

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		mintTransaction.setSignedData(fileUtil.consumeFile(signedFilename));

		String cborHex = new JSONObject(mintTransaction.getSignedData()).getString("cborHex");
		mintTransaction.setTxSize((long) (cborHex.length() / 2));

		fileUtil.removeFile(skeyFilename);
		fileUtil.removeFile(rawFilename);
	}

	private void submitTransaction(MintTransaction mintTransaction) throws Exception {
		String filename = filename("signed");
		try {

			fileUtil.writeFile(filename, mintTransaction.getSignedData());

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
			fileUtil.removeFile(filename);
		}
	}

	private String getTxId(MintTransaction mintTransaction) throws Exception {

		String filename = filename("raw");
		fileUtil.writeFile(filename, mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("txid");
		cmd.add("--tx-body-file");
		cmd.add(filename);
		String txId = ProcessUtil.runCommand(cmd.toArray(new String[0]));

		fileUtil.removeFile(filename);

		return txId;
	}

	private String filename(String ext) {
		return UUID.randomUUID().toString() + "." + ext;
	}

}
