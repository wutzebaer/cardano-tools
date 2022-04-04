package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	//@Column(columnDefinition = "VARBINARY(52428800)")
	byte[] logo;

	int decimals;
}
