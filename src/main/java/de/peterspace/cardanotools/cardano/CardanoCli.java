package de.peterspace.cardanotools.cardano;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.PolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardanoCli {

	private static final Long oneAda = 1_000_000l;
	private static final Pattern lovelacePattern = Pattern.compile("Lovelace (\\d+)");
	private static final Pattern missingLovelacePattern = Pattern.compile("Lovelace \\(-(\\d+)\\)");
	private static final Pattern ipfsPattern = Pattern.compile("Qm[1-9A-Za-z]{44}");

	@Value("${network}")
	private String network;

	@Value("${pledge-address}")
	private String pledgeAddress;

	@Value("${cardano-node.version}")
	private String nodeVersion;

	private final CardanoNode cardanoNode;
	private final AccountRepository accountRepository;
	private final PolicyRepository policyRepository;
	private final IpfsClient ipfsClient;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final CardanoCliDockerBridge cardanoCliDockerBridge;

	private String protocolJson;
	private String dummyAddress;
	private Utxo dummyUtxo = new Utxo();

	@PostConstruct
	public void init() throws Exception {

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("query");
		cmd.add("protocol-parameters");
		cmd.add("--out-file");
		cmd.add("protocol.json");
		protocolJson = cardanoCliDockerBridge.requestCardanoCli(null, cmd.toArray(new String[0]), "protocol.json")[1];

		if (network.equals("preview")) {
			dummyAddress = "addr_test1qp8cprhse9pnnv7f4l3n6pj0afq2hjm6f7r2205dz0583ed6zj0zugmep9lxtuxq8unn85csx9g70ugq6dklmvq6pv3qa0n8cl";
			dummyUtxo.setTxHash("c4b58dd8e4637098c7af36f024006abf305a9848b09332b9095aea3f411cdb74");
			dummyUtxo.setTxIndex(0);
			dummyUtxo.setValue(10000000000l);
		} else if (network.equals("mainnet")) {
			dummyAddress = "addr1q9h7988xmmpz2y50rg2n9fw6jd5rq95t8q84k4q6ne403nxahea9slntm5n8f06nlsynyf4m6sa0qd05agra0qgk09nq96rqh9";
			dummyUtxo.setTxHash("67cbc59640ce98d1f580a211f4c205b0ac8d19c6db96f78b8904462ad588786b");
			dummyUtxo.setTxIndex(0);
			dummyUtxo.setValue(43784400235l);
		} else {
			throw new RuntimeException("Network must be preview or mainnet");
		}

	}

	public long calculateMinUtxo(String addressValue) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add("transaction");
		cmd.add("calculate-min-required-utxo");

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		if ("Babbage".equals(cardanoNode.getEra())) {
			cmd.add("--babbage-era");
		}

		cmd.add("--tx-out");
		cmd.add(addressValue + "+" + oneAda);

		String feeString = cardanoCliDockerBridge.requestCardanoCliNomagic(Map.of("protocol.json", protocolJson), cmd.toArray(new String[0]))[0];
		long fee = Long.valueOf(feeString.split(" ")[1]);

		return fee;
	}

	public Account createAccount() throws Exception {
		String key = UUID.randomUUID().toString();
		Address address = createAddress();
		Account account = Account.builder()
				.key(key)
				.createdAt(new Date())
				.address(address)
				.freePin(false)
				.build();
		accountRepository.save(account);
		return account;
	}

	public Address createAddress() throws Exception {

		String skeyFilename = filename("skey");
		String vkeyFilename = filename("vkey");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("address");
		cmd.add("key-gen");
		cmd.add("--signing-key-file");
		cmd.add(skeyFilename);
		cmd.add("--verification-key-file");
		cmd.add(vkeyFilename);
		String[] keyGenResponse = cardanoCliDockerBridge.requestCardanoCliNomagic(null, cmd.toArray(new String[0]), skeyFilename, vkeyFilename);

		cmd = new ArrayList<String>();
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(vkeyFilename);
		String addressLiteral = cardanoCliDockerBridge.requestCardanoCli(Map.of(vkeyFilename, keyGenResponse[2]), cmd.toArray(new String[0]))[0];

		Address address = new Address(addressLiteral, keyGenResponse[1], keyGenResponse[2], 0l, "[]");
		return address;
	}

	public Policy createPolicy(Account account, long tip, int days) throws Exception {

		Address address = createAddress();

		long secondsToLive = 60 * 60 * 24 * days;
		long dueSlot = tip + secondsToLive;

		String vkeyFilename = filename("vkey");

		// address hash
		ArrayList<String> cmd1 = new ArrayList<String>();
		cmd1.add("address");
		cmd1.add("key-hash");
		cmd1.add("--payment-verification-key-file");
		cmd1.add(vkeyFilename);
		String keyHash = cardanoCliDockerBridge.requestCardanoCliNomagic(Map.of(vkeyFilename, address.getVkey()), cmd1.toArray(new String[0]))[0];

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

		String policyFilename = filename("script");
		String policyString = script.toString(3);
		ArrayList<String> cmd2 = new ArrayList<String>();
		cmd2.add("transaction");
		cmd2.add("policyid");
		cmd2.add("--script-file");
		cmd2.add(policyFilename);
		String policyId = cardanoCliDockerBridge.requestCardanoCliNomagic(Map.of(policyFilename, policyString), cmd2.toArray(new String[0]))[0];

		return Policy.builder()
				.account(account)
				.address(address)
				.policy(policyString)
				.policyId(policyId)
				.policyDueSlot(dueSlot)
				.build();

	}

	public Transaction buildMintTransaction(MintOrderSubmission mintOrderSubmission, Account account) throws Exception {
		List<Utxo> transactionInputs = cardanoDbSyncClient.getUtxos(account.getAddress().getAddress());
		Policy policy = policyRepository.getByAccountAndPolicyId(account, mintOrderSubmission.getPolicyId());
		String returnAddress = cardanoDbSyncClient.getReturnAddress(mintOrderSubmission.getTargetAddress());

		// fake if account is not funded
		if (transactionInputs.size() == 0) {
			transactionInputs.add(dummyUtxo);
		}

		if (StringUtils.isBlank(returnAddress)) {
			mintOrderSubmission.setTargetAddress(dummyAddress);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs();

		// add tokens to mint
		for (TokenSubmission ts : mintOrderSubmission.getTokens()) {
			transactionOutputs.add(returnAddress, formatCurrency(policy.getPolicyId(), Hex.encodeHexString(ts.getAssetName().getBytes())), ts.getAmount());
		}
		// return tokens if user sent some
		transactionInputs.stream().filter(e -> e.getMaPolicyId() != null).forEach(i -> {
			transactionOutputs.add(returnAddress, formatCurrency(i.getMaPolicyId(), i.getMaName()), i.getValue());
		});

		// minutxo
		long minUtxo = 0;
		String cliFormat = transactionOutputs.toCliFormat(returnAddress);
		if (!StringUtils.isBlank(cliFormat)) {
			minUtxo = calculateMinUtxo(cliFormat);
			transactionOutputs.add(returnAddress, "", minUtxo);
		}

		long pinFee = 0;
		if (mintOrderSubmission.getPin() && !account.getFreePin()) {
			Set<String> ipfsUrls = getIpfsUrls(mintOrderSubmission.getMetaData());
			long size = 0;
			for (String url : ipfsUrls) {
				if (!StringUtils.isBlank(url)) {
					size += ipfsClient.getSize(url);
				}
			}
			pinFee += (long) Math.max(size * 0.04, 1_000_000);
			transactionOutputs.add(pledgeAddress, "", pinFee);
		}

		Transaction mintTransaction;
		try {
			mintTransaction = buildTransaction(transactionInputs, transactionOutputs, mintOrderSubmission.getMetaData(), policy, returnAddress);
			signTransaction(mintTransaction, account.getAddress(), policy.getAddress());
		} catch (MissingLovelaceException e) {
			if (transactionInputs.contains(dummyUtxo)) {
				mintTransaction = new Transaction();
				long availableFunds = calculateAvailableFunds(transactionInputs);
				long calculatedMissing = minUtxo + pinFee - availableFunds;
				long missingDifference = e.getAmount() - calculatedMissing;
				mintTransaction.setFee(missingDifference);
			} else {
				transactionInputs.add(dummyUtxo);
				try {
					mintTransaction = buildTransaction(transactionInputs, transactionOutputs, mintOrderSubmission.getMetaData(), policy, returnAddress);
					signTransaction(mintTransaction, account.getAddress(), policy.getAddress());
				} catch (MissingLovelaceException e2) {
					mintTransaction = new Transaction();
					long availableFunds = calculateAvailableFunds(transactionInputs);
					long calculatedMissing = minUtxo + pinFee - availableFunds;
					long missingDifference = e2.getAmount() - calculatedMissing;
					mintTransaction.setFee(missingDifference);
				}
			}
		}

		mintTransaction.setMintOrderSubmission(mintOrderSubmission);
		mintTransaction.setMinOutput(minUtxo);
		mintTransaction.setPinFee(pinFee);
		return mintTransaction;
	}

	public Transaction buildTransaction(Address address, TransactionOutputs transactionOutputs, String metaData) throws Exception {
		List<Utxo> transactionInputs = cardanoDbSyncClient.getUtxos(address.getAddress());

		// fake if account is not funded
		if (transactionInputs.size() == 0) {
			transactionInputs.add(dummyUtxo);
		}

		String changeAddress = transactionOutputs.getOutputs().keySet().iterator().next();

		Transaction mintTransaction;
		try {
			mintTransaction = buildTransaction(transactionInputs, transactionOutputs, metaData, null, changeAddress);
		} catch (MissingLovelaceException e) {
			transactionInputs.add(dummyUtxo);
			mintTransaction = buildTransaction(transactionInputs, transactionOutputs, metaData, null, changeAddress);
		}

		signTransaction(mintTransaction, address);

		return mintTransaction;
	}

	public String mint(List<Utxo> transactionInputs, TransactionOutputs transactionOutputs, JSONObject metaData, Address paymentAddress, Policy policy, String changeAddress) throws Exception {
		Transaction tx = buildTransaction(transactionInputs, transactionOutputs, metaData != null ? metaData.toString(3) : null, policy, changeAddress);

		MintOrderSubmission mintOrderSubmission = new MintOrderSubmission();
		mintOrderSubmission.setMetaData(metaData != null ? metaData.toString(3) : null);
		mintOrderSubmission.setPin(true);
		tx.setMintOrderSubmission(mintOrderSubmission);

		if (policy != null) {
			signTransaction(tx, paymentAddress, policy.getAddress());
		} else {
			signTransaction(tx, paymentAddress);
		}
		String txId = getTxId(tx);
		submitTransaction(tx);
		return txId;
	}

	public Transaction buildTransaction(List<Utxo> transactionInputs, TransactionOutputs transactionOutputs, String metaData, Policy policy, String changeAddress) throws Exception {

		Map<String, String> inputFiles = new HashMap<>();
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add("transaction");
		cmd.add("build");

		if ("Babbage".equals(cardanoNode.getEra())) {
			cmd.add("--babbage-era");
		}

		cmd.add("--change-address");
		cmd.add(changeAddress);

		cmd.add("--witness-override");
		cmd.add(policy == null ? "1" : "2");

		for (Utxo utxo : transactionInputs) {
			cmd.add("--tx-in");
			cmd.add(utxo.getTxHash() + "#" + utxo.getTxIndex());
		}

		for (String a : transactionOutputs.toCliFormat()) {
			cmd.add("--tx-out");
			cmd.add(a);
		}

		List<String> mints = new ArrayList<String>();

		Set<String> outputAssets = transactionOutputs.getOutputs().values()
				.stream()
				.flatMap(a -> a.keySet().stream())
				.filter(e -> !StringUtils.isBlank(e))
				.collect(Collectors.toSet());
		Set<String> inputAssets = transactionInputs
				.stream()
				.filter(e -> e.getMaPolicyId() != null)
				.map(e -> formatCurrency(e.getMaPolicyId(), e.getMaName()))
				.collect(Collectors.toSet());
		Set<String> allAssets = new HashSet<>();
		allAssets.addAll(outputAssets);
		allAssets.addAll(inputAssets);
		for (String assetEntry : allAssets) {
			long inputAmount = transactionInputs.stream().filter(i -> Objects.equals(formatCurrency(i.getMaPolicyId(), i.getMaName()), assetEntry)).mapToLong(i -> i.getValue()).sum();
			long outputAmount = transactionOutputs.getOutputs().values().stream().flatMap(a -> a.entrySet().stream()).filter(e -> Objects.equals(e.getKey(), assetEntry)).mapToLong(e -> e.getValue()).sum();
			long needed = outputAmount - inputAmount;
			if (needed != 0) {
				mints.add(String.format("%d %s", needed, assetEntry));
			}
		}

		if (mints.size() > 0) {
			String policyScriptFilename = filename("script");
			cmd.add("--mint");
			cmd.add(StringUtils.join(mints, "+"));
			cmd.add("--minting-script-file");
			cmd.add(policyScriptFilename);
			inputFiles.put(policyScriptFilename, policy.getPolicy());
		}

		if (metaData != null) {
			String metadataFilename = filename("metadata");
			cmd.add("--json-metadata-no-schema");
			cmd.add("--metadata-json-file");
			cmd.add(metadataFilename);
			inputFiles.put(metadataFilename, metaData);
		}

		String txUnsignedFilename = filename("unsigned");
		cmd.add("--out-file");
		cmd.add(txUnsignedFilename);

		if (policy != null) {
			cmd.add("--invalid-hereafter");
			cmd.add("" + policy.getPolicyDueSlot());
		}

		String[] txResponse;
		try {
			txResponse = cardanoCliDockerBridge.requestCardanoCli(inputFiles, cmd.toArray(new String[0]), txUnsignedFilename);
		} catch (Exception e) {
			log.warn("first mint attempt failed: {}", e.getMessage());
			String message = StringUtils.trimToEmpty(e.getMessage());
			if (message.contains("(change output)")) {
				Matcher matcher = lovelacePattern.matcher(e.getMessage());
				matcher.find();
				Long missingFunds = Long.valueOf(matcher.group(1));
				transactionOutputs.add(transactionOutputs.getOutputs().keySet().iterator().next(), "", missingFunds);
				return buildTransaction(transactionInputs, transactionOutputs, metaData, policy, changeAddress);
			} else if (message.contains("The net balance of the transaction is negative")) {
				Matcher matcher = missingLovelacePattern.matcher(e.getMessage());
				matcher.find();
				long missingFunds = Long.parseLong(matcher.group(1));
				throw new MissingLovelaceException(missingFunds, e.getMessage(), e);
			} else {
				throw e;
			}
		}

		Transaction transaction = new Transaction();
		String[] feeStringChunks = txResponse[0].split(" ");
		transaction.setFee(Long.valueOf(feeStringChunks[feeStringChunks.length - 1]));
		transaction.setInputs(transactionInputs.toString());
		transaction.setOutputs(transactionOutputs.toString());
		if (metaData != null) {
			transaction.setMetaDataJson(metaData);
		}
		transaction.setRawData(txResponse[1]);

		String txId = getTxId(transaction);
		transaction.setTxId(txId);

		return transaction;
	}

	private String formatCurrency(String policyId, String assetNameHex) {
		if (StringUtils.isBlank(assetNameHex)) {
			return policyId;
		} else {
			return policyId + "." + assetNameHex;
		}
	}

	private void signTransaction(Transaction mintTransaction, Address... addresses) throws Exception {

		Map<String, String> inputFiles = new HashMap<>();

		List<String> skeyFilenames = new ArrayList<String>();
		for (Address address : addresses) {
			String skeyFilename = filename("skey");
			skeyFilenames.add(skeyFilename);
			inputFiles.put(skeyFilename, address.getSkey());
		}

		String rawFilename = filename("raw");
		inputFiles.put(rawFilename, mintTransaction.getRawData());

		String signedFilename = filename("signed");

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("transaction");
		cmd.add("sign");

		for (String skeyFilename : skeyFilenames) {
			cmd.add("--signing-key-file");
			cmd.add(skeyFilename);
		}

		cmd.add("--tx-body-file");
		cmd.add(rawFilename);

		cmd.add("--out-file");
		cmd.add(signedFilename);

		String[] output = cardanoCliDockerBridge.requestCardanoCliNomagic(inputFiles, cmd.toArray(new String[0]), signedFilename);

		mintTransaction.setSignedData(output[1]);

		String cborHex = new JSONObject(mintTransaction.getSignedData()).getString("cborHex");
		mintTransaction.setTxSize((long) (cborHex.length() / 2));
	}

	public void submitTransaction(Transaction mintTransaction) throws Exception {
		String filename = filename("signed");
		try {

			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add("transaction");
			cmd.add("submit");

			cmd.add("--tx-file");
			cmd.add(filename);

			// pin files
			if (mintTransaction.getMintOrderSubmission() != null && mintTransaction.getMintOrderSubmission().getPin()) {
				String metaData = mintTransaction.getMintOrderSubmission().getMetaData();
				if (!StringUtils.isBlank(metaData)) {
					Set<String> ipfsUrls = getIpfsUrls(metaData);
					for (String image : ipfsUrls) {
						if (!StringUtils.isBlank(image)) {
							log.info("Pinning {}", image);
							ipfsClient.pinFile(image);
						}
					}
				}
			}

			cardanoCliDockerBridge.requestCardanoCli(Map.of(filename, mintTransaction.getSignedData()), cmd.toArray(new String[0]));

		} catch (Exception e) {
			String message = StringUtils.defaultIfEmpty(e.getMessage(), "");
			if (message.contains("BadInputsUTxO")) {
				throw new Exception("You have unprocessed transactions, please wait a minute.");
			} else if (message.contains("OutsideValidityIntervalUTxO")) {
				throw new Exception("You policy has expired. Confirm to generate a new one.");
			} else {
				throw e;
			}
		}
	}

	Set<String> getIpfsUrls(String metaData) {
		DocumentContext jsonContext = JsonPath.parse(metaData);
		Set<Object> images = new HashSet<Object>();
		images.addAll(jsonContext.read("$.*.*.*.image"));
		images.addAll(jsonContext.read("$.*.*.*.files[*].src"));

		return images.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.map(s -> {
					Matcher matcher = ipfsPattern.matcher(s);
					if (matcher.find()) {
						return matcher.group();
					} else {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private String getTxId(Transaction mintTransaction) throws Exception {
		String filename = filename("raw");
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("transaction");
		cmd.add("txid");
		cmd.add("--tx-body-file");
		cmd.add(filename);
		String txId = cardanoCliDockerBridge.requestCardanoCliNomagic(Map.of(filename, mintTransaction.getRawData()), cmd.toArray(new String[0]))[0];
		return txId;
	}

	private String filename(String ext) {
		return UUID.randomUUID().toString() + "." + ext;
	}

	private long calculateAvailableFunds(List<Utxo> transactionInputs) {
		return transactionInputs.stream().filter(e -> e.getMaPolicyId() == null).mapToLong(e -> e.getValue()).sum();
	}

}
