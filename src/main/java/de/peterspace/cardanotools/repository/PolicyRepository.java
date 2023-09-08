package de.peterspace.cardanotools.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
	Policy getByAccountAndPolicyId(Account account, String policyId);
}