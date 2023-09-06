package de.peterspace.cardanotools.rest.dto;

import jakarta.validation.constraints.NotBlank;
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
