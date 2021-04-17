package de.peterspace.cardanominter.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanominter.model.MintCoinOrder;

@Repository
public interface MintCoinOrderRepository extends PagingAndSortingRepository<MintCoinOrder, String> {

}