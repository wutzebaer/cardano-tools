package de.peterspace.cardanotools.rest.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MetaValue {
	private String value;
	@NotNull
	private List<String> listValue;
}
