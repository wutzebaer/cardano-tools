package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.RegistrationMetadata;

@Repository
public interface RegistrationMetadataRepository extends PagingAndSortingRepository<RegistrationMetadata, Long> {

	boolean existsByPolicyIdAndAssetName(String policyId, String assetName);

}