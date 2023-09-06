package de.peterspace.cardanotools.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class StakePosition {
	private long funds;
	@NotNull
	@Column(nullable = false)
	private String poolHash;
	@NotNull
	@Column(nullable = false)
	private String tickerName;
	private long totalStake;
}
