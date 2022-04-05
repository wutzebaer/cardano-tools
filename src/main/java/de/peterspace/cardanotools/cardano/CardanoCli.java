package de.peterspace.cardanotools.cardano;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.model.Transaction;
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
	private final IpfsClient ipfsClient;

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

	@Cacheable("queryTip")
	@Deprecated
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
		Address address = createAddress();
		Account account = Account.builder()
				.key(key)
				.createdAt(new Date())
				.address(address)
				.fundingAddresses(new ArrayList<>())
				.fundingAddressesHistory(new ArrayList<>())
				.policies(new ArrayList<>())
				.stake(0l)
				.stakePositions(List.of())
				.lastUpdate(new Date())
				.build();
		accountRepository.save(account);
		return account;
	}

	public Address createAddress() throws Exception {

		String skeyFilename = filename("skey");
		String vkeyFilename = filename("vkey");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("key-gen");
		cmd.add("--verification-key-file");
		cmd.add(vkeyFilename);
		cmd.add("--signing-key-file");
		cmd.add(skeyFilename);
		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(vkeyFilename);
		cmd.addAll(List.of(networkMagicArgs));
		String addressLiteral = ProcessUtil.runCommand(cmd.toArray(new String[0]));

		String skey = fileUtil.consumeFile(skeyFilename);
		String vkey = fileUtil.consumeFile(vkeyFilename);

		Address address = new Address(addressLiteral, skey, vkey, 0l, "[]");
		return address;
	}

	public JSONObject getUtxo(Address address) throws Exception {

		String utxoFilename = filename("utxo");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("query");
		cmd.add("utxo");
		cmd.add("--address");
		cmd.add(address.getAddress());
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

	public Transaction buildMintTransaction(MintOrderSubmission mintOrderSubmission, Account account) throws Exception {
		JSONObject utxo = getUtxo(account.getAddress());

		// fake if account is not funded
		if (utxo.length() == 0) {
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAddress).put("value", new JSONObject().put("lovelace", 1000000000l)));
		}

		if (StringUtils.isBlank(mintOrderSubmission.getTargetAddress())) {
			mintOrderSubmission.setTargetAddress(dummyAddress);
		}

		Policy policy = account.getPolicy(mintOrderSubmission.getPolicyId());

		String metadataFilename = createMetadataFile(mintOrderSubmission, new JSONObject(policy.getPolicy()), policy.getPolicyId());
		List<String> mints = createMintList(mintOrderSubmission, policy.getPolicyId());
		long balance = calculateBalance(utxo);
		long minOutput = mintOrderSubmission.getTokens().stream().filter(t -> t.getAmount() > 0).findAny().isPresent() ? MinOutputCalculator.calculate(
				mintOrderSubmission.getTokens().stream().filter(t -> t.getAmount() > 0).map(t -> t.getAssetName()).collect(Collectors.toSet()),
				1l) : 0l;
		long maxSlot = policy.getPolicyDueSlot();
		String scriptFilename = filename("script");
		fileUtil.writeFile(scriptFilename, policy.getPolicy());
		TransactionOutputs transactionOutputs = createMintTransactionOutputs(mintOrderSubmission, utxo, 0, balance, policy.getPolicyId(), minOutput);

		Transaction mintTransaction = createTransaction(transactionOutputs, utxo, 0l, metadataFilename, mints, scriptFilename, maxSlot);

		long fee = calculateFee(mintTransaction, utxo, 2);
		long neededBalance = minOutput + fee + (mintOrderSubmission.getTip() ? 1000000 : 0);
		if (!utxo.has("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0") && account.getAddress().getBalance() < neededBalance) {
			// simulate a further input, because the user has to make another utxo
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAddress).put("value", new JSONObject().put("lovelace", 1000000000l)));
			transactionOutputs = createMintTransactionOutputs(mintOrderSubmission, utxo, fee, balance, policy.getPolicyId(), minOutput);
			mintTransaction = createTransaction(transactionOutputs, utxo, 0, metadataFilename, mints, scriptFilename, maxSlot);
			fee = calculateFee(mintTransaction, utxo, 2);
		}

		transactionOutputs = createMintTransactionOutputs(mintOrderSubmission, utxo, fee, balance, policy.getPolicyId(), minOutput);
		mintTransaction = createTransaction(transactionOutputs, utxo, fee, metadataFilename, mints, scriptFilename, maxSlot);

		fileUtil.removeFile(scriptFilename);
		fileUtil.removeFile(metadataFilename);

		signTransaction(mintTransaction, account.getAddress(), policy.getAddress());

		mintTransaction.setMintOrderSubmission(mintOrderSubmission);
		mintTransaction.setMinOutput(minOutput);
		return mintTransaction;
	}

	public Transaction buildTransaction(Address address, TransactionOutputs transactionOutputs) throws Exception {
		JSONObject utxo = getUtxo(address);

		if (utxo.length() == 0) {
			utxo.put("0f4533c49ee25821af3c2597876a1e9a9cc63ad5054dc453c4e4dc91a9cd7210#0", new JSONObject().put("address", dummyAddress).put("value", new JSONObject().put("lovelace", 1000000000l)));
		}

		Transaction mintTransaction = createTransaction(transactionOutputs, utxo, 0l, null, null, null, null);
		long fee = calculateFee(mintTransaction, utxo, 1);

		mintTransaction = createTransaction(transactionOutputs, utxo, fee, null, null, null, null);

		signTransaction(mintTransaction, address);

		return mintTransaction;
	}

	public Policy createPolicy(Account account, long tip, int days) throws Exception {

		Address address = createAddress();

		long secondsToLive = 60 * 60 * 24 * days;
		long dueSlot = tip + secondsToLive;

		String vkeyFilename = filename("vkey");
		fileUtil.writeFile(vkeyFilename, address.getVkey());

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

		return Policy.builder()
				.account(account)
				.address(address)
				.policy(policyString)
				.policyId(policyId)
				.policyDueSlot(dueSlot)
				.build();

	}

	private Transaction createTransaction(TransactionOutputs transactionOutputs, JSONObject utxo, long fee, String metadataFilename, List<String> mints, String scriptFilename, Long maxSlot) throws Exception {

		String rawFilename = filename("raw");

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

		for (String a : transactionOutputs.toCliFormat()) {
			cmd.add("--tx-out");
			cmd.add(a);
		}

		if (mints != null) {
			cmd.add("--mint");
			cmd.add(StringUtils.join(mints, "+"));
		}

		if (scriptFilename != null) {
			cmd.add("--minting-script-file");
			cmd.add(scriptFilename);
		}

		if (metadataFilename != null) {
			cmd.add("--json-metadata-no-schema");
			cmd.add("--metadata-json-file");
			cmd.add(metadataFilename);
		}

		cmd.add("--out-file");
		cmd.add(rawFilename);

		if (maxSlot != null) {
			cmd.add("--invalid-hereafter");
			cmd.add("" + maxSlot);
		}

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		Transaction transaction = new Transaction();
		transaction.setFee(fee);
		transaction.setInputs(utxo.toString(3));
		transaction.setOutputs(new JSONObject(transactionOutputs.getOutputs()).toString(3));
		if (metadataFilename != null) {
			transaction.setMetaDataJson(fileUtil.readFile(metadataFilename));
		}
		transaction.setRawData(fileUtil.readFile(rawFilename));

		String txId = getTxId(transaction);
		transaction.setTxId(txId);

		fileUtil.removeFile(rawFilename);

		return transaction;
	}

	private TransactionOutputs createMintTransactionOutputs(MintOrderSubmission mintOrderSubmission, JSONObject utxo, long fee, final long balance, final String policyId, long minOutput) throws DecoderException {
		TransactionOutputs transactionOutputs = new TransactionOutputs();
		// add ada change and new minted coins
		if (mintOrderSubmission.getTip()) {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), "", minOutput);
		} else {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), "", balance - fee);
		}
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), formatCurrency(policyId, token.getAssetName()), token.getAmount());
		}

		// the account might have other minted tokens, which also has to be sent
		Iterator<String> utxoKeys = utxo.keys();
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
						// readUtxo already returns hex asset names
						transactionOutputs.add(mintOrderSubmission.getTargetAddress(), otherPolicyId + "." + otherPolicyToken, amount);
					}
				}
			}
		}

		if (mintOrderSubmission.getTip()) {
			long change = balance - minOutput - fee;
			transactionOutputs.add(pledgeAddress, "", Math.max(change, 0));
		}
		return transactionOutputs;
	}

	private List<String> createMintList(MintOrderSubmission mintOrderSubmission, final String policyId) {
		List<String> mints = new ArrayList<String>();
		for (TokenSubmission token : mintOrderSubmission.getTokens()) {
			mints.add(String.format("%d %s", token.getAmount(), formatCurrency(policyId, token.getAssetName())));
		}
		return mints;
	}

	private String formatCurrency(String policyId, String assetName) {
		if (StringUtils.isBlank(assetName)) {
			return policyId;
		} else {
			return policyId + "." + Hex.encodeHexString(assetName.getBytes(StandardCharsets.UTF_8));
		}
	}

	private String createMetadataFile(MintOrderSubmission mintOrderSubmission, final JSONObject policyScript, final String policyId) throws Exception {
		String metadataFilename = filename("metadata");
		fileUtil.writeFile(metadataFilename, mintOrderSubmission.getMetaData());
		return metadataFilename;
	}

	private long calculateFee(Transaction mintTransaction, JSONObject utxo, int witnessCount) throws Exception {
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
		cmd.add("" + witnessCount);

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		String feeString = ProcessUtil.runCommand(cmd.toArray(new String[0]));
		long fee = Long.valueOf(feeString.split(" ")[0]);

		fileUtil.removeFile(filename);

		return fee;
	}

	private void signTransaction(Transaction mintTransaction, Address... addresses) throws Exception {

		List<String> skeyFilenames = new ArrayList<String>();
		for (Address address : addresses) {
			String skeyFilename = filename("skey");
			skeyFilenames.add(skeyFilename);
			fileUtil.writeFile(skeyFilename, address.getSkey());
		}

		String rawFilename = filename("raw");
		fileUtil.writeFile(rawFilename, mintTransaction.getRawData());

		String signedFilename = filename("signed");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(List.of(cardanoCliCmd));
		cmd.add("transaction");
		cmd.add("sign");

		for (String skeyFilename : skeyFilenames) {
			cmd.add("--signing-key-file");
			cmd.add(skeyFilename);
		}

		cmd.addAll(List.of(networkMagicArgs));

		cmd.add("--tx-body-file");
		cmd.add(rawFilename);

		cmd.add("--out-file");
		cmd.add(signedFilename);

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		mintTransaction.setSignedData(fileUtil.consumeFile(signedFilename));

		String cborHex = new JSONObject(mintTransaction.getSignedData()).getString("cborHex");
		mintTransaction.setTxSize((long) (cborHex.length() / 2));

		for (String skeyFilename : skeyFilenames) {
			fileUtil.removeFile(skeyFilename);
		}
		fileUtil.removeFile(rawFilename);
	}

	public void submitTransaction(Transaction mintTransaction) throws Exception {
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

			// pin files
			String metaData = mintTransaction.getMintOrderSubmission().getMetaData();
			if (!StringUtils.isBlank(metaData)) {

				DocumentContext jsonContext = JsonPath.parse(metaData);

				Set<Object> images = new HashSet<Object>();
				images.addAll(jsonContext.read("$.*.*.*.image"));
				images.addAll(jsonContext.read("$.*.*.*.files[*].src"));

				images.stream().filter(String.class::isInstance).map(String.class::cast).forEach(image -> {
					try {
						ipfsClient.pinFile(image);
					} catch (Exception e) {
						log.warn("Could not pin {}: {}", image, e.getMessage());
					}
				});

			}

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

	private String getTxId(Transaction mintTransaction) throws Exception {

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
