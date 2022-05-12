package de.peterspace.cardanotools.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.Views.Persisted;
import de.peterspace.cardanotools.model.Views.Transient;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.DropRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drop")
public class DropRestInterface {

	private final DropRepository dropRepository;
	private final TokenRegistry tokenRegistry;
	private final AccountRepository accountRepository;
	private final CardanoCli cardanoCli;

	@PostMapping("{key}/{policyId}")
	public ResponseEntity<Void> createDrop(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @JsonView(Transient.class) @RequestBody Drop drop) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		Policy policy = account.get().getPolicy(policyId);
		drop.setPolicy(policy);
		drop.setAddress(cardanoCli.createAddress());
		drop.setDropNftsAvailableAssetNames(drop.getDropNfts().stream().map(n -> n.getAssetName()).collect(Collectors.toSet()));
		dropRepository.save(drop);
		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@PutMapping("{key}/{policyId}/{dropId}")
	public ResponseEntity<Void> updateDrop(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @PathVariable("dropId") Long dropId, @JsonView(Transient.class) @RequestBody Drop drop) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		Policy policy = account.get().getPolicy(policyId);

		Drop persistentDrop = dropRepository.findByPolicyAndId(policy, dropId);
		persistentDrop.setRunning(drop.isRunning());
		persistentDrop.setName(drop.getName());
		persistentDrop.setPrice(drop.getPrice());
		persistentDrop.setMaxPerTransaction(drop.getMaxPerTransaction());
		persistentDrop.setProfitAddress(drop.getProfitAddress());
		persistentDrop.setWhitelist(drop.getWhitelist());
		persistentDrop.setDropNfts(drop.getDropNfts());

		persistentDrop.setDropNftsAvailableAssetNames(drop.getDropNfts().stream().map(n -> n.getAssetName()).collect(Collectors.toSet()));
		persistentDrop.getDropNftsAvailableAssetNames().removeAll(persistentDrop.getDropNftsSoldAssetNames());

		dropRepository.save(persistentDrop);
		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@GetMapping("{key}/{policyId}")
	public ResponseEntity<List<Drop>> getDrops(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<List<Drop>>(HttpStatus.NOT_FOUND);
		}
		Policy policy = account.get().getPolicy(policyId);

		List<Drop> drops = dropRepository.findByPolicy(policy);

		return new ResponseEntity<List<Drop>>(drops, HttpStatus.OK);
	}

}
