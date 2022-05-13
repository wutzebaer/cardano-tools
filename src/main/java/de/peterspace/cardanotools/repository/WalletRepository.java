package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Wallet;

@Repository
public interface WalletRepository extends PagingAndSortingRepository<Wallet, Long> {

}