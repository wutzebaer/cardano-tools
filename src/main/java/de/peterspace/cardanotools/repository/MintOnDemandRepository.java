package de.peterspace.cardanotools.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.MintOnDemand;
import de.peterspace.cardanotools.model.Policy;

@Repository
public interface MintOnDemandRepository extends PagingAndSortingRepository<MintOnDemand, Long> {

	MintOnDemand findByPolicy(Policy policy);

	@Transactional
	void deleteByPolicy(Policy policy);

}