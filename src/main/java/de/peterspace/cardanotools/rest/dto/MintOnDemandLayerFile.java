package de.peterspace.cardanotools.rest.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MintOnDemandLayerFile {
	@NotBlank
	private String name;
	@NotNull
	private Double weight;
}
