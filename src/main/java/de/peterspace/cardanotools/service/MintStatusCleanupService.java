package de.peterspace.cardanotools.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.MintingStatus;
import de.peterspace.cardanotools.repository.MintingStatusRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MintStatusCleanupService {

	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final MintingStatusRepository mintingStatusRepository;

	@Scheduled(cron = "*/10 * * * * *")
	public void cleanupStatus() {
		List<MintingStatus> all = mintingStatusRepository.findAllByFinishedFalse();
		long dbTip = cardanoDbSyncClient.getTip();
		for (MintingStatus mintingStatus : all) {
			String txId = mintingStatus.getTxId();
			if (txId != null && mintingStatus.getFinalStep() && cardanoDbSyncClient.isTransactionConfirmed(txId)) {
				mintingStatus.setFinished(true);
				mintingStatusRepository.save(mintingStatus);
			} else if (mintingStatus.getValidUntilSlot() < dbTip) {
				mintingStatusRepository.delete(mintingStatus);
			}
		}
	}

}
