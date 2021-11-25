package de.peterspace.cardanotools.rest;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

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
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.TokenData;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.TokenOffer;
import de.peterspace.cardanotools.model.TokenOfferPost;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.TokenOfferRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeRestInterface {

	private final static long MIN_STAKE = 0;// 95000000l;
	private final CardanoCli cardanoCli;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final TokenOfferRepository tokenOfferRepository;

	private void refreshAddress(Address address) throws Exception {
		address.setBalance(cardanoDbSyncClient.getBalance(address.getAddress()));
		address.setTokensData(new ObjectMapper().writeValueAsString(cardanoDbSyncClient.addressTokens(address.getAddress())));
	}

	@GetMapping("offers")
	@Cacheable("getOffers")
	public List<TokenOffer> getOffers() throws Exception {
		return tokenOfferRepository.findByCanceledIsFalseAndTransactionIsNull();
	}

	@GetMapping("offer/{id}")
	@Cacheable("getOffer")
	public ResponseEntity<TokenOffer> getOffer(@PathVariable("id") Long id) throws Exception {
		Optional<TokenOffer> findById = tokenOfferRepository.findById(id);
		if (!findById.isPresent()) {
			return new ResponseEntity<TokenOffer>(HttpStatus.NOT_FOUND);
		}

		TokenOffer tokenOffer = findById.get();
		refreshAddress(tokenOffer.getAddress());
		tokenOfferRepository.save(tokenOffer);
		return new ResponseEntity<TokenOffer>(tokenOffer, HttpStatus.OK);
	}

	@GetMapping("offerableTokens/{key}")
	@Cacheable("getOfferableTokens")
	public ResponseEntity<List<TokenData>> getOfferableTokens(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			List<TokenData> offerableTokens = cardanoDbSyncClient.getOfferableTokens(account.getAddress().getAddress());
			return new ResponseEntity<List<TokenData>>(offerableTokens, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<TokenData>>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("offerToken/{key}")
	public ResponseEntity<Void> postOfferToken(@PathVariable("key") UUID key, @Valid @RequestBody TokenOfferPost tokenOfferPost) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();

			// min stake
			if (cardanoDbSyncClient.getCurrentStake(account.getAddress().getAddress()) < MIN_STAKE) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
			}

			// check if user is owning the token to sell
			List<TokenData> offerableTokens = cardanoDbSyncClient.getOfferableTokens(account.getAddress().getAddress());
			Optional<TokenData> tokenData = offerableTokens.stream().filter(t -> t.getPolicyId().equals(tokenOfferPost.getPolicyId()) && t.getName().equals(tokenOfferPost.getAssetName())).findAny();
			if (!tokenData.isPresent()) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
			}

			// save offer
			TokenOffer tokenOffer = tokenOfferRepository.findByAccountAndPolicyIdAndAssetNameAndTransactionIsNull(account, tokenOfferPost.getPolicyId(), tokenOfferPost.getAssetName());
			if (tokenOffer == null) {
				tokenOffer = new TokenOffer();
				tokenOffer.setAccount(account);
				tokenOffer.setPolicyId(tokenOfferPost.getPolicyId());
				tokenOffer.setAssetName(tokenOfferPost.getAssetName());
				tokenOffer.setAddress(cardanoCli.createAddress());
			}
			tokenOffer.setCreatedAt(new Date());
			tokenOffer.setPrice(tokenOfferPost.getPrice());
			tokenOffer.setCanceled(tokenOfferPost.getCanceled());
			tokenOffer.setTokenData(new ObjectMapper().writeValueAsString(tokenData.get()));
			tokenOfferRepository.save(tokenOffer);

			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("offeredTokens/{key}")
	@Cacheable("getOfferedTokens")
	public ResponseEntity<List<TokenOffer>> getOfferedTokens(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			List<TokenOffer> findByAccount = tokenOfferRepository.findByAccountAndCanceledIsFalseAndTransactionIsNullOrErrorIsNotNull(account);
			return new ResponseEntity<List<TokenOffer>>(findByAccount, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<TokenOffer>>(HttpStatus.NOT_FOUND);
		}
	}

}
