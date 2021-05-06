package de.peterspace.cardanotools.rest.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRegistration {
	@NotBlank
	private String filename;
	@NotBlank
	private String content;
}
