package de.peterspace.cardanotools;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.model.TransactionInputs;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;

@SpringBootTest
public class CardanoCliTests {

	private static final Address testAddress = new Address("addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", "{\"type\": \"PaymentSigningKeyShelley_ed25519\", \"description\": \"Payment Signing Key\", \"cborHex\": \"5820a210dfed41a028bb2bf4b9a7569b23c4c19a354ab6c167f7604827e56d145a14\"}", "{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}", 0l, "[]");

	@Autowired
	CardanoCli cardanoCli;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	MintTransactionRepository mintTransactionRepository;

	@Autowired
	CardanoDbSyncClient cardanoDbSyncClient;

	@Test
	void tipQuery() throws Exception {
		long tip = CardanoUtil.currentSlot();
		assertThat(tip).isGreaterThan(0);
	}

	@Test
	void createAccount() throws Exception {
		cardanoCli.createAccount();
	}

	@Test
	void getUtxoWithValidAccountKey() throws Exception {
		String key = cardanoCli.createAccount().getKey();
		Account account = accountRepository.findById(key).get();
		long balance = cardanoDbSyncClient.getBalance(account.getAddress().getAddress());
		assertEquals(0, balance);
	}

	public Account createAccount(String key, Address address) throws Exception {
		Account account = Account.builder()
				.key(key)
				.createdAt(new Date())
				.address(address)
				.fundingAddresses(new ArrayList<>())
				.fundingAddressesHistory(new ArrayList<>())
				.policies(new ArrayList<>())
				.stake(0l)
				.lastUpdate(new Date())
				.freePin(false)
				.stakePositions(List.of())
				.build();
		accountRepository.save(account);
		return account;
	}

	@Test
	void getBalanceWithDepositedAccountKey() throws Exception {
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(createAccount(key, testAddress));
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		Account account = accountRepository.findById(key).get();
		long balance = cardanoDbSyncClient.getBalance(account.getAddress().getAddress());
		assertThat(balance).isGreaterThan(1_000_000);
	}

	@Test
	void calculateFee() throws Exception {

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(createAccount(key, testAddress));
		Account account = accountRepository.findById(key).get();

		Policy policy = cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), 7);

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setPolicyId(policy.getPolicyId());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1000000l);
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAmount(1000000l);
		token2.setAssetName("BBBB");
		tokens.add(token2);

		mintOrder.setTokens(tokens);
		mintOrder.setMetaData("{'721':{'" + policy.getPolicyId() + "':{'AAAAA':{haha: 'hoho', hahalist:['list1', 'list2']}, 'BBBB':{}}}}");

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);

		assertThat(mintTransaction.getFee()).isGreaterThan(189000);
	}

	@Test
	void mintCoin() throws Exception {

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(createAccount(key, testAddress));
		Account account = accountRepository.findById(key).get();

		Policy policy = cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), 7);
		account.getPolicies().add(policy);
		account = accountRepository.save(account);

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setPolicyId(policy.getPolicyId());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1000000l);
		token1.setAssetName("XXXXYYYY");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAmount(1000000l);
		token2.setAssetName("");
		tokens.add(token2);

		mintOrder.setTokens(tokens);
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setMetaData("{\"721\":{\"" + policy.getPolicyId() + "\":{\"XXXXYYYY\":{\"haha\": \"hoho\", \"hahalist\":[\"list1\", \"list2\"]}, \"\":{\"name\":\"yay\"}}}}");

		account.getAddress().setBalance(cardanoDbSyncClient.getBalance(account.getAddress().getAddress()));

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);

		mintTransaction.setAccount(account);

		cardanoCli.submitTransaction(mintTransaction);

		mintTransactionRepository.save(mintTransaction);
		assertNotNull(token1.getId());
		assertNotNull(token2.getId());

	}

	@Test
	void mintCip27() throws Exception {

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(createAccount(key, testAddress));
		Account account = accountRepository.findById(key).get();

		Policy policy = cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), 7);
		account.getPolicies().add(policy);
		account = accountRepository.save(account);

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setPolicyId(policy.getPolicyId());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1l);
		token1.setAssetName("");
		tokens.add(token1);

		mintOrder.setTokens(tokens);
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setMetaData("{ \"777\": { \"pct\": \"0.2\", \"addr\": \"addr1v9nevxg9wunfck0gt7hpxuy0elnqygglme3u6l3nn5q5gnq5dc9un\" } }");

		account.getAddress().setBalance(cardanoDbSyncClient.getBalance(account.getAddress().getAddress()));

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);

		cardanoCli.submitTransaction(mintTransaction);

		mintTransaction.setAccount(account);
		mintTransactionRepository.save(mintTransaction);
		assertNotNull(token1.getId());

	}

	@Test
	void sendAda() throws Exception {

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(createAccount(key, testAddress));
		Account account = accountRepository.findById(key).get();

		long balance = cardanoDbSyncClient.getBalance(account.getAddress().getAddress());

		TransactionOutputs transactionOutputs = new TransactionOutputs();
		transactionOutputs.add(account.getAddress().getAddress(), "", balance);

		List<TransactionInputs> utxos = cardanoDbSyncClient.getAddressUtxos(account.getAddress().getAddress());

		// return input tokens to seller
		if (utxos.stream().filter(e -> !e.getPolicyId().isEmpty()).map(f -> f.getPolicyId()).distinct().count() > 0) {
			utxos.stream().filter(e -> !e.getPolicyId().isEmpty()).forEach(i -> {
				transactionOutputs.add(account.getAddress().getAddress(), formatCurrency(i.getPolicyId(), i.getAssetNameBytes()), i.getValue());
			});
		}

		Transaction transaction = cardanoCli.buildTransaction(account.getAddress(), transactionOutputs, null);

		cardanoCli.submitTransaction(transaction);

	}

	private String formatCurrency(String policyId, byte[] assetNameBytes) {
		if (isEmpty(assetNameBytes)) {
			return policyId;
		} else {
			return policyId + "." + Hex.encodeHexString(assetNameBytes);
		}
	}

}
