package de.peterspace.cardanotools.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

}