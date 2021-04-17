package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Account;

@Repository
public interface AccountRepository extends PagingAndSortingRepository<Account, String> {

}