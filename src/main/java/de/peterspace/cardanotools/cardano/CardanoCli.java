package de.peterspace.cardanotools.cardano;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.model.TransactionInputs;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardanoCli {

	private static final Pattern lovelacePattern = Pattern.compile("Lovelace (\\d+)");
	private static final Pattern missingLovelacePattern = Pattern.compile("Lovelace \\(-(\\d+)\\)");
	private static final Pattern ipfsPattern = Pattern.compile("Qm[1-9A-Za-z]{44}");

	@Value("${network}")
	private String network;

	@Value("${pledge-address}")
	private String pledgeAddress;

	@Value("${working.dir}")
	private String workingDir;

	@Value("${cardano-node.version}")
	private String nodeVersion;

	private final CardanoNode cardanoNode;
	private final AccountRepository accountRepository;
	private final FileUtil fileUtil;
	private final IpfsClient ipfsClient;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final CardanoCliDockerBridge cardanoCliDockerBridge;

	private String dummyAddress;
	private TransactionInputs dummyUtxo;

	@PostConstruct
	public void init() throws Exception {

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("query");
		cmd.add("protocol-parameters");
		cmd.add("--out-file");
		cmd.add("protocol.json");
		cardanoCliDockerBridge.requestCardanoCli(cmd.toArray(new String[0]));

		if (network.equals("testnet")) {
			dummyAddress = "addr_test1qpqxjsh6jx0mumxgw5nc5jeu5xy07k35v6h2zutyka72yua578p0hapx37mcflefvvwyhwtwn4kt83nkf7wqwx9tvsdsv8p9ac";
			dummyUtxo = new TransactionInputs("ece82f3f9e1af88ee07fd6be680594c1e357d8e5641afba18e0cd2fda54daedd", 0, 1000_000_000, 0, "", "", "", "");
		} else if (network.equals("mainnet")) {
			dummyAddress = "addr1q9h7988xmmpz2y50rg2n9fw6jd5rq95t8q84k4q6ne403nxahea9slntm5n8f06nlsynyf4m6sa0qd05agra0qgk09nq96rqh9";
			dummyUtxo = new TransactionInputs("b329c5993d883b4d39a5de82762167fbc284ba999a8ebca72ee39dddaeb8b1b8", 0, 43784400235l, 0, "", "", "", "");
		} else {
			throw new RuntimeException("Network must be testnet or mainnet");
		}

	}

	public long calculateMinUtxo(String addressValue) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add("transaction");
		cmd.add("calculate-min-required-utxo");

		cmd.add("--protocol-params-file");
		cmd.add("protocol.json");

		if ("Babbage".equals(cardanoNode.getEra())) {
			// cmd.add("--babbage-era");
		}

		cmd.add("--tx-out");
		cmd.add(addressValue);

		String feeString = cardanoCliDockerBridge.requestCardanoCliNomagic(cmd.toArray(new String[0]));
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
				.fundingAddresses(new ArrayList<>())
				.fundingAddressesHistory(new ArrayList<>())
				.policies(new ArrayList<>())
				.stake(0l)
				.stakePositions(List.of())
				.lastUpdate(new Date())
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
		cmd.add("--verification-key-file");
		cmd.add(vkeyFilename);
		cmd.add("--signing-key-file");
		cmd.add(skeyFilename);
		cardanoCliDockerBridge.requestCardanoCliNomagic(cmd.toArray(new String[0]));

		cmd = new ArrayList<String>();
		cmd.add("address");
		cmd.add("build");
		cmd.add("--payment-verification-key-file");
		cmd.add(vkeyFilename);
		String addressLiteral = cardanoCliDockerBridge.requestCardanoCli(cmd.toArray(new String[0]));

		String skey = fileUtil.consumeFile(skeyFilename);
		String vkey = fileUtil.consumeFile(vkeyFilename);

		Address address = new Address(addressLiteral, skey, vkey, 0l, "[]");
		return address;
	}

	public Policy createPolicy(Account account, long tip, int days) throws Exception {

		Address address = createAddress();

		long secondsToLive = 60 * 60 * 24 * days;
		long dueSlot = tip + secondsToLive;

		String vkeyFilename = filename("vkey");
		fileUtil.writeFile(vkeyFilename, address.getVkey());

		// address hash
		ArrayList<String> cmd1 = new ArrayList<String>();
		cmd1.add("address");
		cmd1.add("key-hash");
		cmd1.add("--payment-verification-key-file");
		cmd1.add(vkeyFilename);
		String keyHash = cardanoCliDockerBridge.requestCardanoCliNomagic(cmd1.toArray(new String[0]));

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
		cmd2.add("transaction");
		cmd2.add("policyid");
		cmd2.add("--script-file");
		cmd2.add(policyFilename);
		String policyId = cardanoCliDockerBridge.requestCardanoCliNomagic(cmd2.toArray(new String[0]));
		fileUtil.removeFile(policyFilename);

		return Policy.builder()
				.account(account)
				.address(address)
				.policy(policyString)
				.policyId(policyId)
				.policyDueSlot(dueSlot)
				.build();

	}

	public Transaction buildMintTransaction(MintOrderSubmission mintOrderSubmission, Account account) throws Exception {
		List<TransactionInputs> transactionInputs = cardanoDbSyncClient.getAddressUtxos(account.getAddress().getAddress());
		Policy policy = account.getPolicy(mintOrderSubmission.getPolicyId());

		// fake if account is not funded
		if (transactionInputs.size() == 0) {
			transactionInputs.add(dummyUtxo);
		}

		if (StringUtils.isBlank(mintOrderSubmission.getTargetAddress())) {
			mintOrderSubmission.setTargetAddress(dummyAddress);
		}

		TransactionOutputs transactionOutputs = new TransactionOutputs();

		// add tokens to mint
		for (TokenSubmission ts : mintOrderSubmission.getTokens()) {
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), formatCurrency(policy.getPolicyId(), ts.getAssetName()), ts.getAmount());
		}
		// return tokens if user sent some
		if (transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).map(f -> f.getPolicyId()).distinct().count() > 0) {
			transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).forEach(i -> {
				transactionOutputs.add(mintOrderSubmission.getTargetAddress(), formatCurrency(i.getPolicyId(), i.getAssetName()), i.getValue());
			});
		}
		// minutxo
		long minUtxo = 0;
		String cliFormat = transactionOutputs.toCliFormat(mintOrderSubmission.getTargetAddress());
		if (!StringUtils.isBlank(cliFormat)) {
			minUtxo = calculateMinUtxo(cliFormat);
			transactionOutputs.add(mintOrderSubmission.getTargetAddress(), "", minUtxo);
		}

		// tip
		String changeAddress;
		long tip = 0;
		if (mintOrderSubmission.getTip()) {
			changeAddress = pledgeAddress;
			tip += 1_000_000;
		} else {
			changeAddress = mintOrderSubmission.getTargetAddress();
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
			mintTransaction = buildTransaction(transactionInputs, transactionOutputs, mintOrderSubmission.getMetaData(), policy, changeAddress);
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
					mintTransaction = buildTransaction(transactionInputs, transactionOutputs, mintOrderSubmission.getMetaData(), policy, changeAddress);
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
		List<TransactionInputs> transactionInputs = cardanoDbSyncClient.getAddressUtxos(address.getAddress());

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

	public String mint(List<TransactionInputs> transactionInputs, TransactionOutputs transactionOutputs, JSONObject metaData, Address paymentAddress, Policy policy, String changeAddress) throws Exception {
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

	public Transaction buildTransaction(List<TransactionInputs> transactionInputs, TransactionOutputs transactionOutputs, String metaData, Policy policy, String changeAddress) throws Exception {

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

		for (TransactionInputs utxo : transactionInputs) {
			cmd.add("--tx-in");
			cmd.add(utxo.getTxhash() + "#" + utxo.getTxix());
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
				.filter(e -> !StringUtils.isBlank(e.getPolicyId()))
				.map(e -> formatCurrency(e.getPolicyId(), e.getAssetName()))
				.collect(Collectors.toSet());
		Set<String> allAssets = new HashSet<>();
		allAssets.addAll(outputAssets);
		allAssets.addAll(inputAssets);
		for (String assetEntry : allAssets) {
			long inputAmount = transactionInputs.stream().filter(i -> Objects.equals(formatCurrency(i.getPolicyId(), i.getAssetName()), assetEntry)).mapToLong(i -> i.getValue()).sum();
			long outputAmount = transactionOutputs.getOutputs().values().stream().flatMap(a -> a.entrySet().stream()).filter(e -> Objects.equals(e.getKey(), assetEntry)).mapToLong(e -> e.getValue()).sum();
			long needed = outputAmount - inputAmount;
			if (needed != 0) {
				mints.add(String.format("%d %s", needed, assetEntry));
			}
		}

		String policyScriptFilename = filename("script");
		if (mints.size() > 0) {
			fileUtil.writeFile(policyScriptFilename, policy.getPolicy());
			cmd.add("--mint");
			cmd.add(StringUtils.join(mints, "+"));
			cmd.add("--minting-script-file");
			cmd.add(policyScriptFilename);
		}

		String metadataFilename = filename("metadata");
		if (metaData != null) {
			fileUtil.writeFile(metadataFilename, metaData);
			cmd.add("--json-metadata-no-schema");
			cmd.add("--metadata-json-file");
			cmd.add(metadataFilename);
		}

		String txUnsignedFilename = filename("unsigned");
		cmd.add("--out-file");
		cmd.add(txUnsignedFilename);

		if (policy != null) {
			cmd.add("--invalid-hereafter");
			cmd.add("" + policy.getPolicyDueSlot());
		}

		String feeString;
		try {
			feeString = cardanoCliDockerBridge.requestCardanoCli(cmd.toArray(new String[0]));
		} catch (Exception e) {
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
		} finally {
			if (metaData != null) {
				fileUtil.removeFile(metadataFilename);
			}
			if (mints.size() > 0) {
				fileUtil.removeFile(policyScriptFilename);
			}
		}

		Transaction transaction = new Transaction();
		String[] feeStringChunks = feeString.split(" ");
		transaction.setFee(Long.valueOf(feeStringChunks[feeStringChunks.length - 1]));
		transaction.setInputs(transactionInputs.toString());
		transaction.setOutputs(transactionOutputs.toString());
		if (metaData != null) {
			transaction.setMetaDataJson(metaData);
		}
		transaction.setRawData(fileUtil.consumeFile(txUnsignedFilename));

		String txId = getTxId(transaction);
		transaction.setTxId(txId);

		return transaction;
	}

	private String formatCurrency(String policyId, String assetName) {
		if (StringUtils.isBlank(assetName)) {
			return policyId;
		} else {
			return policyId + "." + Hex.encodeHexString(assetName.getBytes(StandardCharsets.UTF_8));
		}
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

		cardanoCliDockerBridge.requestCardanoCliNomagic(cmd.toArray(new String[0]));

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

			cardanoCliDockerBridge.requestCardanoCli(cmd.toArray(new String[0]));

		} catch (Exception e) {
			String message = StringUtils.defaultIfEmpty(e.getMessage(), "");
			if (message.contains("BadInputsUTxO")) {
				throw new Exception("You have unprocessed transactions, please wait a minute.");
			} else if (message.contains("OutsideValidityIntervalUTxO")) {
				throw new Exception("You policy has expired. Confirm to generate a new one.");
			} else {
				throw e;
			}
		} finally {
			fileUtil.removeFile(filename);
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
		fileUtil.writeFile(filename, mintTransaction.getRawData());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("transaction");
		cmd.add("txid");
		cmd.add("--tx-body-file");
		cmd.add(filename);
		String txId = cardanoCliDockerBridge.requestCardanoCliNomagic(cmd.toArray(new String[0]));

		fileUtil.removeFile(filename);

		return txId;
	}

	private String filename(String ext) {
		return UUID.randomUUID().toString() + "." + ext;
	}

	private long calculateAvailableFunds(List<TransactionInputs> transactionInputs) {
		return transactionInputs.stream().filter(e -> e.getPolicyId().isEmpty()).mapToLong(e -> e.getValue()).sum();
	}

	private long calculateLockedFunds(List<TransactionInputs> g) throws Exception {

		if (g.stream().filter(s -> !s.getPolicyId().isBlank()).findAny().isEmpty()) {
			return 0;
		}

		String addressValue = g.get(0).getSourceAddress() + " " + g.stream()
				.filter(s -> !s.getPolicyId().isBlank())
				.map(s -> (s.getValue() + " " + formatCurrency(s.getPolicyId(), s.getAssetName())).trim())
				.collect(Collectors.joining("+"));

		return calculateMinUtxo(addressValue);
	}

}
