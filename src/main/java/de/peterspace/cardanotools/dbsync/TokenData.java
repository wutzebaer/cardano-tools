package de.peterspace.cardanotools.dbsync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.peterspace.cardanotools.cardano.ProjectRegistry.ProjectMetadata;
import de.peterspace.cardanotools.cardano.TokenRegistry.TokenRegistryMetadata;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenData {

	@NotBlank
	private String policyId;

	@NotBlank
	private String name;

	@NotNull
	private Long quantity;

	@NotBlank
	private String txId;

	private ProjectMetadata projectMetadata;

	@NotBlank
	private String json;

	private Long invalid_before;

	private Long invalid_hereafter;

	@NotNull
	private Long slotNo;

	@NotNull
	private Long blockNo;

	@NotNull
	private Long epochNo;

	@NotNull
	private Long epochSlotNo;

	@NotNull
	private Long tid;

	@NotNull
	private Long mintid;

	private TokenRegistryMetadata tokenRegistryMetadata;

	private String policy;

	@NotNull
	private Long totalSupply;

	@NotNull
	private String fingerprint;

}
