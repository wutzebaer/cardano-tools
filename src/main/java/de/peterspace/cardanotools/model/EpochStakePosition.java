package de.peterspace.cardanotools.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpochStakePosition {
	@NotNull
	private long amount;
	@NotNull
	private String stakeAddress;
	@NotNull
	private String address;
	@Transient
	@NotNull
	private Map<String, Long> outputs = new HashMap<>();
	@NotNull
	private double share;
}
