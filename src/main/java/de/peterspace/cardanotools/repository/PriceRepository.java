package de.peterspace.cardanotools.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

	@Query("SELECT max(date) FROM Price")
	Optional<LocalDate> getLastDate();

}