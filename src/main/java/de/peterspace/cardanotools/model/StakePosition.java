package de.peterspace.cardanotools.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Embeddable
@Data
public class StakePosition {
	private long funds;
	@NotNull
	private String poolHash;
	@NotNull
	private String tickerName;
	private long totalStake;
}
