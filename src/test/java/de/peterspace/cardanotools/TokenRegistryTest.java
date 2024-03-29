package de.peterspace.cardanotools;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.model.TokenSubmission;
import de.peterspace.cardanotools.model.Transaction;
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

		account.getPolicies().add(0, cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), 1));

		MintOrderSubmission mintOrder = new MintOrderSubmission();
		mintOrder.setPin(false);
		mintOrder.setTargetAddress(account.getAddress().getAddress());
		mintOrder.setPolicyId(account.getPolicies().get(0).getPolicyId());
		mintOrder.setPin(false);

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
		mintOrder.setMetaData("{\"721\":{\"" + mintOrder.getPolicyId() + "\":{\"XXXXYYYY\":{\"haha\": \"hoho\", \"hahalist\":[\"list1\", \"list2\"]}, \"\":{\"name\":\"yay\"}}}}");

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrder, account);
		mintTransaction.setAccount(account);
		byte[] logoData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("mini.png"));

		Policy policy = account.getPolicy(mintTransaction.getMintOrderSubmission().getPolicyId());
		String url = tokenRegistry.createTokenRegistration(new RegistrationMetadata(null, "AAAAA", policy.getPolicyId(), policy.getPolicy(), mintTransaction.getAccount().getAddress().getSkey(), "AAANAME", "AAADESC", "AAATICKER", null, logoData, 0));

		log.debug(url);

	}

}
