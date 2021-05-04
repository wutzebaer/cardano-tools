package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
public class RegistrationMetadata {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotBlank
	private String assetName;

	@NotBlank
	private String policyId;

	@NotBlank
	private String policy;

	@NotBlank
	private String policySkey;

	@NotBlank
	private String name;

	@NotBlank
	private String description;

	String ticker;

	String url;

	// kleiner als 64kb und png
	String logo;

}
