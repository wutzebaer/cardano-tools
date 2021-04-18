package de.peterspace.cardanotools.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.MintOrder;

@Repository
public interface MintOrderRepository extends PagingAndSortingRepository<MintOrder, String> {

}