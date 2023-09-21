package de.peterspace.cardanotools.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
