package de.peterspace.cardanotools.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.Policy;

@Repository
public interface DropRepository extends PagingAndSortingRepository<Drop, Long> {

	List<Drop> findByPolicy(Policy policy);

	Drop findByPolicyAndId(Policy policy, Long id);

}