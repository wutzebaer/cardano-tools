package de.peterspace.cardanotools.rest;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.Min;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountRestInterface {

	private final CardanoCli cardanoCli;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@PostMapping
	public Account createAccount() throws Exception {
		Account account = cardanoCli.createAccount();
		refreshAndSaveAccount(account, 7);
		return account;
	}

	@GetMapping("{key}")
	@Cacheable("getAccount")
	public ResponseEntity<Account> getAccount(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			refreshAndSaveAccount(account, 7);
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("{key}/refreshPolicy")
	public ResponseEntity<Account> refreshPolicy(@PathVariable("key") UUID key, @RequestBody @Min(1) Integer days) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			Policy policy = cardanoCli.createPolicy(account, CardanoUtil.currentSlot(), days);
			account.getPolicies().add(0, policy);
			refreshAndSaveAccount(account, days);
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
