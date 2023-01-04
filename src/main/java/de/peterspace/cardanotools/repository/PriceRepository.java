package de.peterspace.cardanotools.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Price;

@Repository
public interface PriceRepository extends PagingAndSortingRepository<Price, Long> {

	@Query("SELECT max(date) FROM Price")
	Optional<LocalDate> getLastDate();

}