package de.peterspace.cardanotools.rest.dto;

import java.util.HashMap;
import java.util.Map;

import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class StakeRewardPosition extends EpochStake {
//	@NotNull
//	private long amount;
//	@NotNull
//	private String stakeAddress;
//	@NotNull
//	private String address;
//	@Transient
	@NotNull
	private Map<String, Long> outputs = new HashMap<>();
	@NotNull
	private double share;
}
