package de.peterspace.cardanominter.model;

import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import com.sun.istack.NotNull;

import lombok.Data;

@Data
public class MintCoinOrder {
	@Id
	private String key;
	@NotBlank
	private String receiver;
	@NotBlank
	private String tokenName;
	@NotNull
	private Long tokenAmount;
	@ElementCollection
	private Map<String, String> metaData;
}