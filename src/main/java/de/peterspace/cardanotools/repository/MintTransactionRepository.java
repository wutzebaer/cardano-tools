package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Transaction;

@Repository
public interface MintTransactionRepository extends PagingAndSortingRepository<Transaction, Long> {

	Transaction findFirstByAccountKeyOrderByIdDesc(String key);

}