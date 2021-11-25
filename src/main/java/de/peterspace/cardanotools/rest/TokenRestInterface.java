package de.peterspace.cardanotools.rest;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.TokenData;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
public class TokenRestInterface {

	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("findTokens")
	@Cacheable("findTokens")
	public ResponseEntity<List<TokenData>> findTokens(@RequestParam String string, @RequestParam(required = false) Long fromTid) throws Exception {
		List<TokenData> findTokens = cardanoDbSyncClient.findTokens(string, fromTid);
		return new ResponseEntity<List<TokenData>>(findTokens, HttpStatus.OK);
	}

	@GetMapping("latestTokens")
	@Cacheable("latestTokens")
	public ResponseEntity<List<TokenData>> latestTokens(@RequestParam(required = false) Long fromMintid) throws Exception {
		List<TokenData> findTokens = cardanoDbSyncClient.latestTokens(fromMintid);
		return new ResponseEntity<List<TokenData>>(findTokens, HttpStatus.OK);
	}

	@GetMapping("walletTokens")
	@Cacheable("walletTokens")
	public ResponseEntity<List<TokenData>> walletTokens(@RequestParam String address) throws Exception {
		List<TokenData> findTokens = cardanoDbSyncClient.walletTokens(address);
		return new ResponseEntity<List<TokenData>>(findTokens, HttpStatus.OK);
	}

}
