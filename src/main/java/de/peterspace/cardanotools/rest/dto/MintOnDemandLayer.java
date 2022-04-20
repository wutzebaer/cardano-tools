package de.peterspace.cardanotools.rest.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MintOnDemandLayer {
	@NotBlank
	private String name;
	@NotNull
	private List<MintOnDemandLayerFile> files;
}
