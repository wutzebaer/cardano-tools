package de.peterspace.cardanotools.rest.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SnapshotRequest {

	@Data
	public static class SnapshotRequestPolicy {
		private String policyId;
	}

	private Date timestamp;
	private List<SnapshotRequestPolicy> policies;
}