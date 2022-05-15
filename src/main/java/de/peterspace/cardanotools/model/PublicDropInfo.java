package de.peterspace.cardanotools.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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