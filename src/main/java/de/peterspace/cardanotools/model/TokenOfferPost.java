package de.peterspace.cardanotools.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class TokenOfferPost {

	@NotBlank
	private String policyId;

	@NotBlank
	private String assetName;

	@NotNull
	@Min(2000000)
	private Long price;

	@NotNull
	private Boolean canceled;

}
