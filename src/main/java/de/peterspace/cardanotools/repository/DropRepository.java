package de.peterspace.cardanotools.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.Policy;

@Repository
public interface DropRepository extends JpaRepository<Drop, Long> {

	List<Drop> findByPolicy(Policy policy);

	Drop findByPolicyAndId(Policy policy, Long id);

	List<Drop> findAll();

	Drop findByPrettyUrl(String prettyUrl);

}