package de.peterspace.cardanotools.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.MintingStatus;
import jakarta.transaction.Transactional;

@Repository
public interface MintingStatusRepository extends JpaRepository<MintingStatus, String> {

	MintingStatus findByPaymentTxId(String paymentTxId);

	List<MintingStatus> findAllByFinishedFalse();

	@Modifying
	@Query("update MintingStatus set status=?2, finished=?3, txId=?4, finalStep=?5 where paymentTxId = ?1")
	@Transactional
	int updateMintingStatus(String paymentTxId, String status, boolean finished, String txId, boolean finalStep);

}