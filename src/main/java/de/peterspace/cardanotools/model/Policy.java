package de.peterspace.cardanotools.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String policyId;

	@NotNull
	@JsonIgnore
	@ManyToOne
	private Account account;

	@NotBlank
	@NotNull
	@Column(columnDefinition = "TEXT")
	private String policy;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

	@NotNull
	private Long policyDueSlot;

}