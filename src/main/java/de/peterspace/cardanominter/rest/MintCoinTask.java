package de.peterspace.cardanominter.rest;

import java.util.Map;

import lombok.Data;

@Data
public class MintCoinTask {
	private String receiver;
	private String tokenName;
	private Long tokenAmount;
	private Map<String, String> metaData;
}