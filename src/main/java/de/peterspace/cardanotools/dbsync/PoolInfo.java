package de.peterspace.cardanotools.dbsync;

import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

@Value
public class PoolInfo {
	@NotEmpty
	String ticker;
	@NotEmpty
	String hash;
}
