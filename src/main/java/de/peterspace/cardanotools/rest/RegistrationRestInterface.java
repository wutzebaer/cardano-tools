package de.peterspace.cardanotools.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.json.JSONStringer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.RegistrationMetadata;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/registration")
public class RegistrationRestInterface {

	private final TokenRegistry tokenRegistry;

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
