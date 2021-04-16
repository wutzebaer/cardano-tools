package de.peterspace.cardanominter.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanominter.model.Address;

@Repository
public interface AddressRepository extends PagingAndSortingRepository<Address, String> {

}