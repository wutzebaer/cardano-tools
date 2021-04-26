package de.peterspace.cardanotools.model;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
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

	@JsonIgnore
	private final String temporaryFilePrefix = UUID.randomUUID().toString();

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotEmpty
	@OneToMany(cascade = CascadeType.ALL)
	private List<TokenSubmission> tokens;

	@NotBlank
	private String targetAddress;

	@NotNull
	private ChangeAction changeAction;

	public String createFilePrefix() {
		return temporaryFilePrefix + "_mintorder";
	}

}