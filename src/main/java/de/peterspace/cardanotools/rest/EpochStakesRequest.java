package de.peterspace.cardanotools.rest;

import java.util.List;

public class EpochStakesRequest {
	public boolean tip;
	public long minStake;
	public String message;
	public List<String> excludedStakers;

	public EpochStakesRequest(boolean tip, long minStake, boolean excludePledge, String message, List<String> excludedStakers) {
		this.tip = tip;
		this.minStake = minStake;
		this.message = message;
		this.excludedStakers = excludedStakers;
	}
}