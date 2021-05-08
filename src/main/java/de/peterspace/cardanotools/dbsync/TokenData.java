package de.peterspace.cardanotools.dbsync;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TokenData {

	@NotBlank
	private String policyId;

	@NotBlank
	private String name;

	@NotNull
	private Long quantity;

	@NotBlank
	private String txId;

	@NotBlank
	private String json;

	private Long invalid_before;

	private Long invalid_hereafter;

	@NotNull
	private Long block_no;

	@NotNull
	private Long epoch_no;

	@NotNull
	private Long epoch_slot_no;

	@NotNull
	private Long tid;
}
