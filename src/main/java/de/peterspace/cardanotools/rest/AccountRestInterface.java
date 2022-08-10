package de.peterspace.cardanotools.rest;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.Views.Private;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountRestInterface {

	private final CardanoCli cardanoCli;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@PostMapping
	@JsonView(Private.class)
	public Account createAccount() throws Exception {
		Account account = cardanoCli.createAccount();
		refreshAndSaveAccount(account, 365);
		return account;
	}

	@GetMapping("{key}")
	@Cacheable("getAccount")
	@JsonView(Private.class)
	public ResponseEntity<Account> getAccount(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			refreshAndSaveAccount(account, 365);
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	@Value
	public static class PolicyConfig {
		private int days;
		private String name;
		private String policyId;
		private String policy;
		private String skey;
	}

	@PostMapping("{key}/refreshPolicy")
	@JsonView(Private.class)
	public ResponseEntity<Account> createNewPolicy(@PathVariable("key") UUID key, @RequestBody PolicyConfig policyConfig) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			Policy policy;
			if (StringUtils.isBlank(policyConfig.getSkey())) {
				policy = cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), policyConfig.days);
				policy.setName(policyConfig.getName());
			} else {
				policy = new Policy();
				policy.setAccount(account);
				policy.setAddress(new Address("???", policyConfig.getSkey(), "???", 0l, "[]"));
				policy.setName(policyConfig.getName());
				policy.setPolicy(policyConfig.getPolicy());
				policy.setPolicyId(policyConfig.getPolicyId());

				JSONArray scriptsArray = new JSONObject(policyConfig.getPolicy()).getJSONArray("scripts");
				for (int i = 0; i < scriptsArray.length(); i++) {
					JSONObject scriptOnject = scriptsArray.getJSONObject(i);
					if (scriptOnject.getString("type").equals("before")) {
						policy.setPolicyDueSlot(scriptOnject.getLong("slot"));
					}
				}

			}
			account.getPolicies().add(0, policy);
			refreshAndSaveAccount(account, policyConfig.days);
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	private void refreshAndSaveAccount(Account account, int days) throws Exception {
		long tip = CardanoUtil.currentSlot();
		if (tip > account.getPolicies().stream().mapToLong(p -> p.getPolicyDueSlot()).max().orElse(0)) {
			Policy policy = cardanoCli.createPolicy(account, tip, days);
			account.getPolicies().add(0, policy);
		}
		refreshAddress(account.getAddress());
		account.setStake(cardanoDbSyncClient.getCurrentStake(account.getAddress().getAddress()));
		account.setStakePositions(cardanoDbSyncClient.allStakes(account.getAddress().getAddress()));
		account.setFundingAddresses(cardanoDbSyncClient.getFundingAddresses(account.getAddress().getAddress()));
		account.setFundingAddressesHistory(cardanoDbSyncClient.getFundingAddressesHistory(account.getAddress().getAddress()));
		account.setLastUpdate(new Date());
		accountRepository.save(account);
	}

	private void refreshAddress(Address address) throws Exception {
		address.setBalance(cardanoDbSyncClient.getBalance(address.getAddress()));
		address.setTokensData(new ObjectMapper().writeValueAsString(cardanoDbSyncClient.addressTokens(address.getAddress())));
	}

}
