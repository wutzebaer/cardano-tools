package de.peterspace.cardanotools.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

}