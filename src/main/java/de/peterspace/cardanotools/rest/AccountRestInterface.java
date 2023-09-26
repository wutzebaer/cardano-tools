package de.peterspace.cardanotools.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.PolicyRepository;
import de.peterspace.cardanotools.rest.dto.Views.Private;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountRestInterface {

	private final CardanoCli cardanoCli;
	private final AccountRepository accountRepository;
	private final PolicyRepository policyRepository;

	@PostMapping
	@JsonView(Private.class)
	public Account createAccount() throws Exception {
		Account account = cardanoCli.createAccount();
		return account;
	}

	@GetMapping("{key}")
	@JsonView(Private.class)
	public ResponseEntity<Account> getAccount(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("{key}/policies")
	@JsonView(Private.class)
	public ResponseEntity<List<Policy>> getPolicies(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			return new ResponseEntity<List<Policy>>(policyRepository.findByAccount(accountOptional.get()), HttpStatus.OK);
		} else {
			return new ResponseEntity<List<Policy>>(HttpStatus.NOT_FOUND);
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

	@PostMapping("{key}/createPolicy")
	@JsonView(Private.class)
	public ResponseEntity<Policy> createNewPolicy(@PathVariable("key") UUID key, @RequestBody PolicyConfig policyConfig) throws Exception {
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
				policy.setAddress(new Address("???", policyConfig.getSkey(), "???"));
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
			policyRepository.save(policy);
			return new ResponseEntity<Policy>(policy, HttpStatus.OK);
		} else {
			return new ResponseEntity<Policy>(HttpStatus.NOT_FOUND);
		}
	}

}
