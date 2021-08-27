package de.peterspace.cardanotools.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.TokenOffer;

@Repository
public interface TokenOfferRepository extends PagingAndSortingRepository<TokenOffer, Long> {

	List<TokenOffer> findByCanceledIsFalseAndTransactionIsNull();

	List<TokenOffer> findByCanceledIsFalseAndTransactionNullAndErrorIsNull();

	List<TokenOffer> findByAccountAndCanceledIsFalseAndTransactionIsNullOrErrorIsNotNull(Account account);

	TokenOffer findByAccountAndPolicyIdAndAssetNameAndTransactionIsNull(Account account, String policyId, String assetName);

}