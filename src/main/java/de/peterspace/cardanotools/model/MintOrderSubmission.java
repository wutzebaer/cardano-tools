package de.peterspace.cardanotools.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private Boolean tip;

	@NotNull
	@Column(columnDefinition = "bool default true")
	private Boolean pin;

	@NotBlank
	@Column(nullable = false)
	private String policyId;

	@Column(columnDefinition = "TEXT")
	private String metaData;

}