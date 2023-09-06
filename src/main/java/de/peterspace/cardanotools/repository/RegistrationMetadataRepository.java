package de.peterspace.cardanotools.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.RegistrationMetadata;

@Repository
public interface RegistrationMetadataRepository extends JpaRepository<RegistrationMetadata, Long> {

	boolean existsByPolicyIdAndAssetName(String policyId, String assetName);

}