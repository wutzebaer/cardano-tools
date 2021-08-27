package de.peterspace.cardanotools.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
