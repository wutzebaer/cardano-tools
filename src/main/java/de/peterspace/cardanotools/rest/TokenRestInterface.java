package de.peterspace.cardanotools.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.cardano.TokenRegistry.TokenRegistryMetadata;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
public class TokenRestInterface {

	private final TokenRegistry tokenRegistry;

	@GetMapping("/{policyId}/{assetName}/tokenRegistryMetadata")
	public ResponseEntity<TokenRegistryMetadata> getTokenRegistryMetadata(@PathVariable String policyId, @PathVariable String assetName) throws Exception {
		String subject = CardanoUtil.createSubject(policyId, assetName);
		return new ResponseEntity<TokenRegistryMetadata>(tokenRegistry.getTokenRegistryMetadata().get(subject), HttpStatus.OK);
	}

}
