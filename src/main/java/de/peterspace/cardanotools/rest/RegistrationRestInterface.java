package de.peterspace.cardanotools.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Policy;
import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/registration")
public class RegistrationRestInterface {

	private final MintTransactionRepository mintTransactionRepository;
	private final AccountRepository accountRepository;
	private final TokenRegistry tokenRegistry;

	@GetMapping("getRegistrationMetadata/{key}")
	@Cacheable("getRegistrationMetadata")
	public ResponseEntity<RegistrationMetadata> getRegistrationMetadata(@PathVariable("key") UUID key) throws Exception {
		Optional<Account> account = accountRepository.findById(key.toString());
		if (!account.isPresent()) {
			return new ResponseEntity<RegistrationMetadata>(HttpStatus.NOT_FOUND);
		}

		Transaction mintTransaction = mintTransactionRepository.findFirstByAccountKeyOrderByIdDesc(key.toString());
		RegistrationMetadata registrationMetadata = new RegistrationMetadata();

		registrationMetadata.setAssetName(mintTransaction.getMintOrderSubmission().getTokens().get(0).getAssetName());

		JSONObject tokenMetaData = new JSONObject(mintTransaction.getMintOrderSubmission().getTokens().get(0).getMetaData());
		if (tokenMetaData.has("name")) {
			registrationMetadata.setName(tokenMetaData.getString("name"));
		}

		Policy policy = account.get().getPolicy(mintTransaction.getMintOrderSubmission().getPolicyId());

		registrationMetadata.setPolicy(policy.getPolicy());
		registrationMetadata.setPolicySkey(policy.getAddress().getSkey());

		return new ResponseEntity<RegistrationMetadata>(registrationMetadata, HttpStatus.OK);
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

}
