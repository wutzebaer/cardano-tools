package de.peterspace.cardanotools.rest.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SnapshotResult {
	@Data
	public static class SnapshotResultRow {

		@Data
		public static class SnapshotResultToken {
			private List<String> assetnames;
			private Long amount;
		}

		private String wallet;
		private List<SnapshotResultToken> snapshotResultTokens;
	}

	private List<SnapshotResultRow> rows;
}