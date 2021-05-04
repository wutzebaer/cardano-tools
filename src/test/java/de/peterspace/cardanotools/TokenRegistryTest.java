package de.peterspace.cardanotools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MetaValue;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.MintTransaction;
import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class TokenRegistryTest {

	@Autowired
	AccountRepository addressRepository;

	@Autowired
	CardanoCli cardanoCli;

	@Autowired
	TokenRegistry tokenRegistry;

	@Test
	void testCreateRegistryEntry() throws Exception {

		Account account = cardanoCli.createAccount();

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setTip(false);
		mintOrder.setTargetAddress(account.getAddress());

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAmount(1000000l);
		token1.setMetaData(Map.of(
				"haha", new MetaValue(0l, "hoho", List.of()),
				"hahalist", new MetaValue(0l, "", List.of("list1", "list2"))));
		token1.setAssetName("AAAAA");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAmount(1000000l);
		token2.setAssetName("BBBB");
		token2.setMetaData(new HashMap<String, MetaValue>());
		tokens.add(token2);

		mintOrder.setTokens(tokens);

		MintTransaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);
		mintTransaction.setAccount(account);

		tokenRegistry.registerToken(new RegistrationMetadata(null, "AAAAA", mintTransaction.getPolicyId(), mintTransaction.getPolicy(), mintTransaction.getAccount().getSkey(), "AAANAME", "AAADESC", "AAATI", null, "are_you_fucking_kidding_me_clean.png"));

	}

}
