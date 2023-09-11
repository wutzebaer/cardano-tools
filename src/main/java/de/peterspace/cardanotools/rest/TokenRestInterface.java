package de.peterspace.cardanotools.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanodbsyncapi.client.model.TokenDetails;
import de.peterspace.cardanodbsyncapi.client.model.TokenListItem;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
public class TokenRestInterface {

	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping
	public ResponseEntity<List<TokenListItem>> getTokens(@RequestParam(required = false) Long afterMintid, @RequestParam(required = false) Long beforeMintid, @RequestParam(required = false) String filter) throws Exception {
		List<TokenListItem> tokenList = cardanoDbSyncClient.getTokenList(afterMintid, beforeMintid, filter);
		return new ResponseEntity<List<TokenListItem>>(tokenList, HttpStatus.OK);
	}

	@GetMapping("/{policyId}/{assetName}")
	public ResponseEntity<TokenDetails> getTokenDetails(@PathVariable String policyId, @PathVariable String assetName) throws Exception {
		TokenDetails tokenDetails = cardanoDbSyncClient.getTokenDetails(policyId, assetName);
		return new ResponseEntity<TokenDetails>(tokenDetails, HttpStatus.OK);
	}

}
