package de.peterspace.cardanotools.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "policyId", "assetName", "account_key", "transaction_id" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenOffer {

	@Id
	@GeneratedValue
	@NotNull
	private Long id;

	@NotBlank
	private String policyId;

	@NotBlank
	private String assetName;

	@NotNull
	private Long price;

	@NotNull
	@JsonIgnore
	@ManyToOne
	private Account account;

	@NotNull
	@JsonIgnore
	private Date createdAt;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

	@NotNull
	private Boolean canceled;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	String tokenData;

	@ManyToOne(cascade = CascadeType.ALL)
	Transaction transaction;

	@Column(columnDefinition = "TEXT")
	String error;

}
