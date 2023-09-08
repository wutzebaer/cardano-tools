package de.peterspace.cardanotools.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintOrderSubmission {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotEmpty
	@OneToMany(cascade = CascadeType.ALL)
	private List<TokenSubmission> tokens;

	@NotBlank
	@Column(nullable = false)
	private String targetAddress;

	@NotNull
	@Column(columnDefinition = "bool default true")
	private Boolean pin;

	@NotBlank
	@Column(nullable = false)
	private String policyId;

	@Column(columnDefinition = "TEXT")
	private String metaData;

}