package de.peterspace.cardanotools.cardano;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.OfferFunding;
import de.peterspace.cardanotools.dbsync.OfferTokenFunding;
import de.peterspace.cardanotools.model.TokenOffer;
import de.peterspace.cardanotools.model.Transaction;
import de.peterspace.cardanotools.repository.TokenOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExchangeService {

	private final TokenOfferRepository tokenOfferRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final CardanoCli cardanoCli;

	//@Scheduled(cron = "*/10 * * * * *")
	public void processOffers() throws Exception {
		for (TokenOffer offer : tokenOfferRepository.findByCanceledIsFalseAndTransactionNullAndErrorIsNull()) {

			// check funds
			List<OfferFunding> offerFundings = cardanoDbSyncClient.getOfferFundings(offer.getAddress().getAddress());
			long totalFunds = offerFundings.stream().mapToLong(of -> of.getFunds()).sum();
			if (totalFunds >= offer.getPrice()) {

				// check token
				List<OfferTokenFunding> offerTokenFundings = cardanoDbSyncClient.getOfferTokenFundings(offer.getAddress().getAddress());
				Optional<OfferTokenFunding> optionalTokenFounder = offerTokenFundings.stream().filter(otf -> otf.getPolicy().equals(offer.getPolicyId()) && otf.getName().equals(offer.getAssetName())).findAny();
				if (optionalTokenFounder.isPresent()) {

					try {
						TransactionOutputs transactionOutputs = new TransactionOutputs();
						OfferFunding winningFounding = offerFundings.stream().sorted(Comparator.comparingLong(OfferFunding::getFunds).reversed()).findFirst().get();

						// pay back non winning founders, which paid at least the full price
						for (OfferFunding offerFunding : offerFundings) {
							if (offerFunding != winningFounding && offerFunding.getFunds() >= offer.getPrice()) {
								transactionOutputs.add(offerFunding.getAddress(), "", offerFunding.getFunds());
								totalFunds -= offerFunding.getFunds();
							}
						}

						// winning founder gets tokens
						for (OfferTokenFunding offerTokenFunding : offerTokenFundings) {
							transactionOutputs.add(winningFounding.getAddress(), offerTokenFunding.getPolicy() + "." + offerTokenFunding.getName(), offerTokenFunding.getQuantity());
						}
						long minOutput = MinOutputCalculator.calculate(
								offerTokenFundings.stream().map(f -> f.getName()).collect(Collectors.toSet()),
								offerTokenFundings.stream().map(f -> f.getPolicy()).distinct().count());
						transactionOutputs.add(winningFounding.getAddress(), "", minOutput);
						totalFunds -= minOutput;

						// token founder gets ada
						transactionOutputs.add(optionalTokenFounder.get().getAddress(), "", totalFunds);


						Transaction buildTransaction = cardanoCli.buildTransaction(offer.getAddress(), transactionOutputs, null);
						buildTransaction.setAccount(offer.getAccount());
						offer.setTransaction(buildTransaction);

						cardanoCli.submitTransaction(buildTransaction);

					} catch (Exception e) {
						offer.setError(e.getMessage());
					}

					tokenOfferRepository.save(offer);

				}
			}
		}
	}

}
