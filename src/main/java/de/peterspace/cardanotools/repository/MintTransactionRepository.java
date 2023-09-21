package de.peterspace.cardanotools.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Transaction;

@Repository
public interface MintTransactionRepository extends JpaRepository<Transaction, Long> {

	Transaction findFirstByAccountKeyOrderByIdDesc(String key);

}