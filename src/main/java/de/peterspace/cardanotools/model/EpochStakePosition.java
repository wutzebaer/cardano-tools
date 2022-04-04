package de.peterspace.cardanotools.model;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class EpochStakePosition {
	@NotNull
	private long amount;
	@NotNull
	private String stakeAddress;
	@NotNull
	private String address;
}
