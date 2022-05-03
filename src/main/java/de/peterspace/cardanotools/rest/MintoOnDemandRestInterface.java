package de.peterspace.cardanotools.rest;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOnDemand;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintOnDemandRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mint-on-demand")
public class MintoOnDemandRestInterface {

	private final MintOnDemandRepository mintOnDemandRepository;
	private final TokenRegistry tokenRegistry;
	private final AccountRepository accountRepository;

	@PostMapping(path = "{key}/{policyId}/{filename}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> saveMintOnDemandFile(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @PathVariable("filename") String filename, @RequestPart MultipartFile file) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		Policy policy = account.get().getPolicy(policyId);

		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@PostMapping("{key}/{policyId}")
	public ResponseEntity<Void> saveMintOnDemand(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @RequestBody MintOnDemand mintOnDemand) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		Policy policy = account.get().getPolicy(policyId);

		mintOnDemand.setPolicy(policy);
		mintOnDemandRepository.deleteByPolicy(policy);
		mintOnDemandRepository.save(mintOnDemand);

		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

}
