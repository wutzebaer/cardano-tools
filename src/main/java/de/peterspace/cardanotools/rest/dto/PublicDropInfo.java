package de.peterspace.cardanotools.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class PublicDropInfo {
	@NotNull
	@NotBlank
	private String name;
	@NotNull
	private int total;
	@NotNull
	private int left;
	@NotNull
	@NotBlank
	private String address;
	@NotNull
	private int max;
	@NotNull
	private long price;
	@NotNull
	private boolean running;
	@NotNull
	@NotBlank
	private String policyId;
}
