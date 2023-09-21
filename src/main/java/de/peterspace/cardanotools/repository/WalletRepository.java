package de.peterspace.cardanotools.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

	Optional<Wallet> findByDropAndStakeAddress(Drop drop, String stakeAddress);

}