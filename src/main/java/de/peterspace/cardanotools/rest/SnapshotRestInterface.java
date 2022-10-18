package de.peterspace.cardanotools.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.rest.dto.SnapshotRequest;
import de.peterspace.cardanotools.rest.dto.SnapshotResult;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/snapshot")
public class SnapshotRestInterface {

	private final CardanoDbSyncClient cardanoDbSyncClient;

	@PostMapping
	public ResponseEntity<SnapshotResult> createSnapshot(@RequestBody SnapshotRequest snapshotRequest) throws Exception {
		SnapshotResult snapshot = cardanoDbSyncClient.createSnapshot(snapshotRequest);
		return new ResponseEntity<>(snapshot, HttpStatus.OK);
	}

}
