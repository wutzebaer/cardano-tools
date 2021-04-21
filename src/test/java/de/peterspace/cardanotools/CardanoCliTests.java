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
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrder;
import de.peterspace.cardanotools.model.Token;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintOrderRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CardanoCliTests {

	@Autowired
	CardanoCli cardanoCli;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	MintOrderRepository mintCoinOrderRepository;

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
			account.setAddress("c:\\");
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
		// https://developers.cardano.org/en/testnets/cardano/tools/faucet/
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(new Account("e69db833-8af7-4bb9-81cf-df04282a41c0", new Date(), "addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", "{\"type\": \"PaymentSigningKeyShelley_ed25519\", \"description\": \"Payment Signing Key\", \"cborHex\": \"5820a210dfed41a028bb2bf4b9a7569b23c4c19a354ab6c167f7604827e56d145a14\"}", "{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}"));
		Account account = accountRepository.findById(key).get();
		JSONObject utxo = cardanoCli.getUtxo(account);
		long balance = cardanoCli.calculateBalance(utxo);
		assertThat(balance).isGreaterThan(1_000_000_000 - 100_000_000);
	}

	@Test
	void calculateFee() throws Exception {
		MintOrder mintOrder = new MintOrder();
		mintOrder.setCreatedAt(new Date());
		mintOrder.setAccount(null);

		ArrayList<Token> tokens = new ArrayList<Token>();

		Token token1 = new Token();
		token1.setAmount(1000000l);
		token1.setMetaDataJson("{'HAAH': 'HOHO'}");
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		Token token2 = new Token();
		token2.setAmount(1000000l);
		token2.setAssetName("BBBB");
		tokens.add(token2);

		mintOrder.setTokens(tokens);

		long fee = cardanoCli.calculateTransactionFee(mintOrder);

		assertEquals(180197, fee);
	}

	@Test
	void mintCoin() throws Exception {
		String key = "e69db833-8af7-4bb9-81cf-df04282a41c0";
		accountRepository.save(new Account("e69db833-8af7-4bb9-81cf-df04282a41c0", new Date(), "addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", "{\"type\": \"PaymentSigningKeyShelley_ed25519\", \"description\": \"Payment Signing Key\", \"cborHex\": \"5820a210dfed41a028bb2bf4b9a7569b23c4c19a354ab6c167f7604827e56d145a14\"}", "{\"type\": \"PaymentVerificationKeyShelley_ed25519\", \"description\": \"Payment Verification Key\", \"cborHex\": \"5820996819facb997e96243124d8717f9fa1867be456c5e649e3bab3d2a68b36e999\"}"));
		Account account = accountRepository.findById(key).get();
		while (cardanoCli.calculateBalance(cardanoCli.getUtxo(account)) < 1) {
			log.info("Please uploads funds with https://developers.cardano.org/en/testnets/cardano/tools/faucet/ to {}", account.getAddress());
			Thread.sleep(1000);
		}
		String receiver = "addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x";

		MintOrder mintOrder = new MintOrder();
		mintOrder.setCreatedAt(new Date());
		mintOrder.setAccount(account);

		ArrayList<Token> tokens = new ArrayList<Token>();

		Token token1 = new Token();
		token1.setMintOrder(mintOrder);
		token1.setAmount(1000000l);
		token1.setMetaDataJson("{'HAAH': 'HOHO'}");
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		Token token2 = new Token();
		token2.setMintOrder(mintOrder);
		token2.setAmount(1000000l);
		token2.setMetaDataJson("{'HAAH': 'HOHO'}");
		token2.setAssetName("BBBB");
		tokens.add(token2);

		mintOrder.setTokens(tokens);

		mintCoinOrderRepository.save(mintOrder);

		assertNotNull(token1.getId());
		assertNotNull(token2.getId());

		cardanoCli.executeMintOrder(mintOrder, receiver);

		new JSONObject(mintOrder.getPolicyScript());
	}

}
