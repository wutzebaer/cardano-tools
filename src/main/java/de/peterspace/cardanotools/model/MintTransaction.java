package de.peterspace.cardanotools.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintTransaction {

	@Id
	private long id;

	@NotBlank
	private String txId;

	@NotNull
	private Long fee;

	@NotBlank
	private String policyId;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String outputs;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String inputs;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String rawData;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String metaDataJson;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String policy;

}
