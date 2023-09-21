package de.peterspace.cardanotools.model;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "policyId", "assetName" }) })
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

	@URL
	String url;

	// kleiner als 64kb und png
	@Size(max = 52428800)
	@JsonIgnore
	// @Column(columnDefinition = "VARBINARY(52428800)")
	byte[] logo;

	int decimals;
}
