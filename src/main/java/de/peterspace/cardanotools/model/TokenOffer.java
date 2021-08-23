package de.peterspace.cardanotools.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "policyId", "assetName", "account_key" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenOffer {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotBlank
	private String policyId;

	@NotBlank
	private String assetName;

	@NotNull
	private Double price;

	@NotNull
	@JsonIgnore
	@ManyToOne
	private Account account;

	@NotNull
	@JsonIgnore
	private Date createdAt;

	@NotNull
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

}
