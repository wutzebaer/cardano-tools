package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.MintTransaction;

@Repository
public interface MintTransactionRepository extends PagingAndSortingRepository<MintTransaction, Long> {

	MintTransaction findFirstByAccountKeyOrderByIdDesc(String key);

}