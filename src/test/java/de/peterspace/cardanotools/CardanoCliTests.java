package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.Policy;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.MintTransaction;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CardanoCliTests {

	private static final Address testAddress = new Address("addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", "{\"type\": \"PaymentSigningKeyShelley_ed25519\", \"description\": \"Payment Signing Key\", \"cborHex\": \"5820a210dfed41a028bb2bf4b9a7569b23c4c19a354ab6c167f7604827e56d145a14\"}", "{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}", 0l, "[]");

	@Autowired
	CardanoCli cardanoCli;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	MintTransactionRepository mintTransactionRepository;

	@Test
	void tipQuery() throws Exception {
		long tip = cardanoCli.queryTip();
		assertThat(tip).isGreaterThan(0);
	}

	@Test
	void createAccount() throws Exception {
		cardanoCli.createAccount();
	}

	@Test
	void getUtxoWithInvalidAccountKey() throws Exception {
		Exception exception = assertThrows(Exception.class, () -> {
			String key = cardanoCli.createAccount().getKey();
			Account account = accountRepository.findById(key).get();
			account.setAddress(new Address("c:\\", "c:\\", "c:\\", 0l, "[]"));
			cardanoCli.getUtxo(account);
		});
		String expectedMessage = "option --address: Failed reading: invalid addressUsage: cardano-cli query utxo [--shelley-mode | --byron-mode                                 [--epoch-slots NATURAL] |                                --cardano-mode [--epoch-slots NATURAL]]                               [(--address ADDRESS)]                               (--mainnet | --testnet-magic NATURAL)                               [--out-file FILE]  Get the node's current UTxO with the option of filtering by address(es)";
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	void getUtxoWithValidAccountKey() throws Exception {
		String key = cardanoCli.createAccount().getKey();
		Account account = accountRepository.findById(key).get();
		JSONObject utxo = cardanoCli.getUtxo(account);
		long balance = cardanoCli.calculateBalance(utxo);
		assertEquals(0, balance);
	}

	@Test
	void getBalanceWithDepositedAccountKey() throws Exception {
		Policy policy = cardanoCli.createPolicy("{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}", cardanoCli.queryTip());
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(new Account("e69db833-8af7-4bb9-81cf-df04282a41c0", new Date(), testAddress, new ArrayList<>(), new ArrayList<>(), 0l, new Date(), policy.getPolicy(), policy.getPolicyId(), policy.getPolicyDueDate()));
		Account account = accountRepository.findById(key).get();
		JSONObject utxo = cardanoCli.getUtxo(account);
		long balance = cardanoCli.calculateBalance(utxo);
		assertThat(balance).isGreaterThan(1_000_000);
	}

	@Test
	void calculateFee() throws Exception {
		Policy policy = cardanoCli.createPolicy("{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}", cardanoCli.queryTip());

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(new Account("e69db833-8af7-4bb9-81cf-df04282a41c0", new Date(), testAddress, new ArrayList<>(), new ArrayList<>(), 0l, new Date(), policy.getPolicy(), policy.getPolicyId(), policy.getPolicyDueDate()));
		Account account = accountRepository.findById(key).get();

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1000000l);
		token1.setMetaData("{haha: 'hoho'}, hahalist:['list1', 'list2']");
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAmount(1000000l);
		token2.setAssetName("BBBB");
		token2.setMetaData("{}");
		tokens.add(token2);

		mintOrder.setTokens(tokens);

		MintTransaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);

		assertThat(mintTransaction.getFee()).isGreaterThan(189000);
	}

	@Test
	void mintCoin() throws Exception {

		Policy policy = cardanoCli.createPolicy("{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}", cardanoCli.queryTip());

		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(new Account("e69db833-8af7-4bb9-81cf-df04282a41c0", new Date(), testAddress, new ArrayList<>(), new ArrayList<>(), 0l, new Date(), policy.getPolicy(), policy.getPolicyId(), policy.getPolicyDueDate()));
		Account account = accountRepository.findById(key).get();

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1000000l);
		token1.setMetaData("{haha: 'hoho'}, hahalist:['list1', 'list2']");
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAmount(1000000l);
		token2.setAssetName("BBBB");
		token2.setMetaData("{}");
		tokens.add(token2);

		mintOrder.setTokens(tokens);
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());

		JSONObject utxo = cardanoCli.getUtxo(account);
		account.getAddress().setBalance(cardanoCli.calculateBalance(utxo));

		MintTransaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);

		mintTransaction.setAccount(account);

		cardanoCli.executeMintTransaction(mintTransaction);

		mintTransactionRepository.save(mintTransaction);
		assertNotNull(token1.getId());
		assertNotNull(token2.getId());

	}

}
