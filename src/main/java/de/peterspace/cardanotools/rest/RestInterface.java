package de.peterspace.cardanotools.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.Policy;
import de.peterspace.cardanotools.cardano.PolicyScanner;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.TokenData;
import de.peterspace.cardanotools.ipfs.IpfsClient;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.model.TokenOffer;
import de.peterspace.cardanotools.model.TokenOfferPost;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import de.peterspace.cardanotools.repository.TokenOfferRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestInterface {

	private final static long MIN_STAKE = 0;//95000000l;
	private final CardanoCli cardanoCli;
	private final IpfsClient ipfsClient;
	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final TokenRegistry tokenRegistry;
	private final PolicyScanner policyScanner;
	private final TokenOfferRepository tokenOfferRepository;

	@GetMapping("tip")
	@Cacheable("getTip")
	public long getTip() throws Exception {
		return cardanoCli.queryTip();
	}

	@PostMapping("Account")
	public Account createAccount() throws Exception {
		Account account = cardanoCli.createAccount();
		return account;
	}

	@GetMapping("account/{key}")
	@Cacheable("getAccount")
	public ResponseEntity<Account> getAccount(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			refreshAccount(account, 7);
			accountRepository.save(account);
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("account/{key}/refreshPolicy")
	public ResponseEntity<Account> refreshPolicy(@PathVariable("key") UUID key, @RequestBody @Min(1) Integer days) throws Exception {
		Optional<Account> accountOptional = accountRepository.findById(key.toString());
		if (accountOptional.isPresent()) {
			Account account = accountOptional.get();
			account.setPolicyDueDate(null);
			refreshAccount(account, days);
			accountRepository.save(account);
			return new ResponseEntity<Account>(accountOptional.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Account>(HttpStatus.NOT_FOUND);
		}
	}

	private void refreshAccount(Account account, int days) throws Exception {
		if (account.getPolicyDueDate() == null || System.currentTimeMillis() > account.getPolicyDueDate().getTime()) {
			Policy policy = cardanoCli.createPolicy(account.getAddress().getVkey(), cardanoCli.queryTip(), days);
			account.setPolicy(policy.getPolicy());
			account.setPolicyId(policy.getPolicyId());
			account.setPolicyDueDate(policy.getPolicyDueDate());
		}
		refreshAddress(account.getAddress());
		account.setStake(cardanoDbSyncClient.getCurrentStake(account.getAddress().getAddress()));
		account.setFundingAddresses(cardanoDbSyncClient.getFundingAddresses(account.getAddress().getAddress()));
		account.setFundingAddressesHistory(cardanoDbSyncClient.getFundingAddressesHistory(account.getAddress().getAddress()));
		account.setLastUpdate(new Date());
	}

	private void refreshAddress(Address address) throws Exception {
		address.setBalance(cardanoDbSyncClient.getBalance(address.getAddress()));
		address.setTokensData(new ObjectMapper().writeValueAsString(cardanoDbSyncClient.addressTokens(address.getAddress())));
	}

	@PostMapping(path = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String postFile(@RequestPart MultipartFile file) throws Exception {
		String ipfsData = ipfsClient.addFile(file.getInputStream());
		return JSONStringer.valueToString(ipfsData);
	}

	@PostMapping("buildMintTransaction/{key}")
	public ResponseEntity<Transaction> buildMintTransaction(@PathVariable("key") UUID key, @RequestBody MintOrderSubmission mintOrderSubmission) throws Exception {

		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Transaction>(HttpStatus.NOT_FOUND);
		}

		if (account.get().getAddress().getBalance() > 0 && !StringUtils.isBlank(mintOrderSubmission.getTargetAddress()) && !account.get().getFundingAddresses().contains(mintOrderSubmission.getTargetAddress())) {
			throw new Exception("Invalid target address.");
		}

		Transaction mintTransaction = cardanoCli.buildMintTransaction(mintOrderSubmission, account.get());
		return new ResponseEntity<Transaction>(mintTransaction, HttpStatus.OK);
	}

	@PostMapping("submitMintTransaction/{key}")
	public ResponseEntity<Void> submitMintTransaction(@PathVariable("key") UUID key, @RequestBody Transaction mintTransaction) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		mintTransaction.setAccount(account.get());
		cardanoCli.submitTransaction(mintTransaction);
		mintTransactionRepository.save(mintTransaction);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@GetMapping("getRegistrationMetadata/{key}")
	@Cacheable("getRegistrationMetadata")
	public ResponseEntity<RegistrationMetadata> getRegistrationMetadata(@PathVariable("key") UUID key) throws Exception {
		Transaction mintTransaction = mintTransactionRepository.findFirstByAccountKeyOrderByIdDesc(key.toString());
		RegistrationMetadata registrationMetadata = new RegistrationMetadata();

		registrationMetadata.setAssetName(mintTransaction.getMintOrderSubmission().getTokens().get(0).getAssetName());

		JSONObject tokenMetaData = new JSONObject(mintTransaction.getMintOrderSubmission().getTokens().get(0).getMetaData());
		if (tokenMetaData.has("name")) {
			registrationMetadata.setName(tokenMetaData.getString("name"));
		}

		registrationMetadata.setPolicyId(mintTransaction.getPolicyId());
		registrationMetadata.setPolicy(mintTransaction.getPolicy());
		registrationMetadata.setPolicySkey(mintTransaction.getAccount().getAddress().getSkey());

		return new ResponseEntity<RegistrationMetadata>(registrationMetadata, HttpStatus.OK);
	}

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

	@PostMapping(path = "generateTokenRegistration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String generateTokenRegistration(@RequestPart String registrationMetadataString, @RequestPart(required = false) MultipartFile file) throws Exception {

		RegistrationMetadata registrationMetadata = new ObjectMapper().readValue(registrationMetadataString, RegistrationMetadata.class);

		if (file != null) {
			BufferedImage image = ImageIO.read(file.getInputStream());
			BufferedImage resized = Scalr.resize(image, Scalr.Mode.AUTOMATIC, 150, 150);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(resized, "png", bos);
			registrationMetadata.setLogo(bos.toByteArray());
		}

		String pullRequestUrl = tokenRegistry.createTokenRegistration(registrationMetadata);

		return JSONStringer.valueToString(pullRequestUrl);
	}

	@GetMapping("policy")
	@Cacheable("policy")
	public String policy(@RequestParam String policyId) throws Exception {
		String policy = policyScanner.getPolicies().get(policyId);
		return policy;
	}

}
