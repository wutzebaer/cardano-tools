package de.peterspace.cardanotools.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	@Column(columnDefinition = "TEXT")
	private String name;

}