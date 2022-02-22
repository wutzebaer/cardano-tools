package de.peterspace.cardanotools.model;

import java.util.Date;

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
public class Transaction {

	@Id
	@GeneratedValue
	@JsonIgnore
	private long id;

	@NotNull
	@JsonIgnore
	@ManyToOne
	private Account account;

	@JsonIgnore
	private Date submitDate;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String signedData;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String rawData;

	@NotBlank
	private String txId;

	@NotNull
	private Long fee;

	private Long minOutput;

	@NotNull
	@Column(columnDefinition = "bigint DEFAULT 0")
	private Long txSize;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String outputs;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String inputs;

	@Column(columnDefinition = "TEXT")
	private String metaDataJson;

	@OneToOne(cascade = CascadeType.ALL)
	MintOrderSubmission mintOrderSubmission;

}
