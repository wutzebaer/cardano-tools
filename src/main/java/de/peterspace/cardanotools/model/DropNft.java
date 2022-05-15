package de.peterspace.cardanotools.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Embeddable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DropNft {

	@NotNull
	@NotEmpty
	@EqualsAndHashCode.Include
	private String assetName;

	@NotNull
	@NotEmpty
	@Column(columnDefinition = "TEXT")
	private String metadata;

}