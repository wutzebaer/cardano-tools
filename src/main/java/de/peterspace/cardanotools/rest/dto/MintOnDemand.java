package de.peterspace.cardanotools.rest.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MintOnDemand {
	@NotNull
	private Integer width;
	@NotNull
	private Integer height;
	@NotBlank
	private String metadataString;
	@NotNull
	private List<MintOnDemandLayer> layers;
}
