package de.peterspace.cardanotools.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	private Long pinFee;

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
