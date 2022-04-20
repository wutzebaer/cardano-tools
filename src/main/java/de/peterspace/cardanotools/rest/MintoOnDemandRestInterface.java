package de.peterspace.cardanotools.rest;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.rest.dto.MintOnDemand;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mint-on-demand")
public class MintoOnDemandRestInterface {

	private final TokenRegistry tokenRegistry;

	@PostMapping(path = "{key}/{policyId}/{filename}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void saveMintOnDemandFile(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @PathVariable("filename") String filename, @RequestPart MultipartFile file) throws Exception {
	}

	@PostMapping("{key}/{policyId}")
	public void saveMintOnDemand(@PathVariable("key") UUID key, @PathVariable("policyId") String policyId, @RequestBody MintOnDemand mintOnDemand) throws Exception {
	}

}
