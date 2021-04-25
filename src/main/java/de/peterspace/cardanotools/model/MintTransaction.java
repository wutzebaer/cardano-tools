package de.peterspace.cardanotools.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintTransaction {

	@Id
	@GeneratedValue
	@JsonIgnore
	private long id;

	@NotNull
	@JsonIgnore
	@ManyToOne
	private Account account;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	@JsonIgnore
	private String signedData;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String rawData;

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
	private String metaDataJson;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String policy;

	@NotNull
	@OneToOne(cascade = CascadeType.ALL)
	MintOrderSubmission mintOrderSubmission;

}
