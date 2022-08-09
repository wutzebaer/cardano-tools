package de.peterspace.cardanotools.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.DropNft;
import de.peterspace.cardanotools.model.TransactionInputs;
import de.peterspace.cardanotools.model.Wallet;
import de.peterspace.cardanotools.repository.DropRepository;
import de.peterspace.cardanotools.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DropperService {

	@Value("${pledge-address}")
	private String pledgeAddress;

	private final SecureRandom sr = new SecureRandom();
	private final DropRepository dropRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final CardanoCli cardanoCli;
	private final TaskExecutor taskExecutor;
	private final WalletRepository walletRepository;

	private final Cache<Long, Boolean> temporaryBlacklist = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
	private final Set<TransactionInputs> permanentBlacklist = new HashSet<>();

	public List<String> findFundedAddresses() {
		List<Drop> drops = dropRepository.findAll();
		return drops
				.stream()
				.map(d -> d.getAddress().getAddress())
				.filter(fundAddress -> {
					List<TransactionInputs> offerFundings = cardanoDbSyncClient.getAddressUtxos(fundAddress);
					Map<Long, List<TransactionInputs>> transactionInputGroups = offerFundings.stream()
							.filter(of -> temporaryBlacklist.getIfPresent(of.getStakeAddressId()) == null)
							.filter(of -> !permanentBlacklist.contains(of))
							.collect(Collectors.groupingBy(of -> of.getStakeAddressId(), LinkedHashMap::new, Collectors.toList()));
					return transactionInputGroups.size() > 0;
				})
				.collect(Collectors.toList());
	}

	@Scheduled(cron = "*/10 * * * * *")
	@Transactional
	public void dropNfts() {
		log.info("START Dropper cycle");

		List<Drop> drops = dropRepository.findAll();

		for (Drop drop : drops) {

			Address fundAddress = drop.getAddress();
			List<TransactionInputs> offerFundings = cardanoDbSyncClient.getAddressUtxos(fundAddress.getAddress());
			Map<Long, List<TransactionInputs>> transactionInputGroups = offerFundings.stream()
					.filter(of -> temporaryBlacklist.getIfPresent(of.getStakeAddressId()) == null)
					.filter(of -> !permanentBlacklist.contains(of))
					.collect(Collectors.groupingBy(of -> of.getStakeAddressId(), LinkedHashMap::new, Collectors.toList()));

			for (List<TransactionInputs> transactionInputs : transactionInputGroups.values()) {
				try {

					Set<Long> whitelist = cardanoDbSyncClient.findStakeAddressIds(drop.getWhitelist().toArray(new String[] {}));
					long lockedFunds = calculateLockedFunds(transactionInputs);

					if (!whitelist.isEmpty() && !whitelist.contains(transactionInputs.get(0).getStakeAddressId()) || !drop.isRunning()) {
						refund(fundAddress, transactionInputs, lockedFunds);
					} else {

						Optional<Wallet> wallet = Optional.empty();
						if (!whitelist.isEmpty()) {
							wallet = walletRepository.findById(transactionInputs.get(0).getStakeAddressId());
							if (wallet.isEmpty()) {
								wallet = Optional.of(new Wallet(transactionInputs.get(0).getStakeAddressId(), 0));
							}
						}

						long price = drop.getPrice();
						int mintsLeft = drop.getMaxPerTransaction() - wallet.map(w -> w.getTokensMinted()).orElse(0);

						if (drop.getDropNftsAvailableAssetNames().size() == 0 || mintsLeft < 1 || drop.getPolicy().getPolicyDueSlot() < CardanoUtil.currentSlot()) {
							refund(fundAddress, transactionInputs, lockedFunds);
						} else if (hasEnoughFunds(price, transactionInputs, lockedFunds)) {
							long funds = calculateAvailableFunds(transactionInputs) - lockedFunds;
							int amount = (int) NumberUtils.min(funds / price, mintsLeft);
							List<DropNft> tokens = findTokens(drop, amount);
							long totalPrice = tokens.size() * price;

							if (wallet.isPresent()) {
								wallet.get().setTokensMinted(wallet.get().getTokensMinted() + tokens.size());
								walletRepository.save(wallet.get());
								log.info("Wallet {} has {} tokens left", wallet.get().getStakeAddressId(), drop.getMaxPerTransaction() - wallet.get().getTokensMinted());
							}

							List<String> usedAssets = tokens.stream().map(t -> t.getAssetName()).collect(Collectors.toList());
							drop.getDropNftsAvailableAssetNames().removeAll(usedAssets);
							drop.getDropNftsSoldAssetNames().addAll(usedAssets);
							dropRepository.save(drop);

							sell(drop, transactionInputs, tokens, totalPrice, lockedFunds);
						}

					}

				} catch (Exception e) {
					log.error("TransactionInputs failed to process", e);
				} finally {
					temporaryBlacklist.put(transactionInputs.get(0).getStakeAddressId(), true);
				}
			}

		}

		log.info("END  Dropper cycle");
	}

	private void sell(Drop drop, List<TransactionInputs> transactionInputs, List<DropNft> tokens, long totalPrice, long lockedFunds) throws Exception {

		taskExecutor.execute(() -> {
			try {
				String buyerAddress = transactionInputs.get(0).getSourceAddress();

				log.info("selling {} tokens to {} : {}", tokens.size(), buyerAddress, tokens);

				TransactionOutputs transactionOutputs = new TransactionOutputs();

				// send tokens
				for (DropNft token : tokens) {
					transactionOutputs.add(buyerAddress, formatCurrency(drop.getPolicy().getPolicyId(), token.getAssetName()), 1);
				}

				// return input tokens to seller
				if (transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).map(f -> f.getPolicyId()).distinct().count() > 0) {
					transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).forEach(i -> {
						transactionOutputs.add(buyerAddress, formatCurrency(i.getPolicyId(), i.getAssetName()), i.getValue());
					});
				}

				// min output for tokens
				transactionOutputs.add(buyerAddress, "", cardanoCli.calculateMinUtxo(transactionOutputs.toCliFormat(buyerAddress)));

				// return change to buyer
				long change = calculateAvailableFunds(transactionInputs) - lockedFunds - totalPrice;
				transactionOutputs.add(buyerAddress, "", change);

				if (true) {
					transactionOutputs.add(pledgeAddress, "", 1_000_000);
				}

				// build metadata
				JSONObject policyMetadata = new JSONObject();
				for (DropNft tokenData : tokens) {
					JSONObject metaData = new JSONObject(tokenData.getMetadata());
					Iterator<String> keys = metaData.keys();
					while (keys.hasNext()) {
						if (keys.next().startsWith("_")) {
							keys.remove();
						}
					}
					policyMetadata.put(tokenData.getAssetName(), metaData);
				}
				JSONObject metaData = new JSONObject().put("721", new JSONObject().put(drop.getPolicy().getPolicyId(), policyMetadata).put("version", "1.0"));

				// submit transaction
				String txId = cardanoCli.mint(transactionInputs, transactionOutputs, metaData, drop.getAddress(), drop.getPolicy(), drop.getProfitAddress());
				permanentBlacklist.addAll(transactionInputs);
				log.info("Successfully sold {} for {}, txid: {}", tokens.size(), totalPrice, txId);
			} catch (Exception e) {
				log.error("sell failed", e);
				Drop returnDrop = dropRepository.findById(drop.getId()).get();
				List<String> usedAssets = tokens.stream().map(t -> t.getAssetName()).collect(Collectors.toList());
				returnDrop.getDropNftsAvailableAssetNames().addAll(usedAssets);
				returnDrop.getDropNftsSoldAssetNames().removeAll(usedAssets);
				dropRepository.save(returnDrop);
				log.error("returned tokens {}", usedAssets);
			}
		});

	}

	private void refund(Address fundAddress, List<TransactionInputs> transactionInputs, long lockedFunds) throws Exception {

		taskExecutor.execute(() -> {
			try {
				// determine amount of tokens
				String buyerAddress = transactionInputs.get(0).getSourceAddress();

				TransactionOutputs transactionOutputs = new TransactionOutputs();

				if (transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).map(f -> f.getPolicyId()).distinct().count() > 0) {
					transactionInputs.stream().filter(e -> !e.getPolicyId().isEmpty()).forEach(i -> {
						transactionOutputs.add(buyerAddress, formatCurrency(i.getPolicyId(), i.getAssetName()), i.getValue());
					});
				}
				transactionOutputs.add(buyerAddress, "", lockedFunds);

				String txId = cardanoCli.mint(transactionInputs, transactionOutputs, null, fundAddress, null, buyerAddress);
				permanentBlacklist.addAll(transactionInputs);
				log.info("Successfully refunded, txid: {}", txId);

			} catch (Exception e) {
				log.error("sell failed", e);
			}
		});

	}

	private List<DropNft> findTokens(Drop drop, int amount) {
		ArrayList<String> availableTokens = new ArrayList<>(drop.getDropNftsAvailableAssetNames());
		Collections.shuffle(availableTokens, sr);
		List<String> choosedTokens = availableTokens.subList(0, Math.min(amount, availableTokens.size()));
		List<DropNft> tokenDatas = choosedTokens.stream()
				.map(assetName -> drop.getDropNfts().stream().filter(nft -> Objects.equals(assetName, nft.getAssetName())).findFirst().get())
				.collect(Collectors.toList());
		return tokenDatas;
	}

	private boolean hasEnoughFunds(long minFunds, List<TransactionInputs> g, long lockedFunds) throws Exception {
		return calculateAvailableFunds(g) - lockedFunds >= (minFunds);
	}

	private long calculateAvailableFunds(List<TransactionInputs> transactionInputs) {
		return transactionInputs.stream().filter(e -> e.getPolicyId().isEmpty()).mapToLong(e -> e.getValue()).sum();
	}

	private long calculateLockedFunds(List<TransactionInputs> g) throws Exception {

		if (g.stream().filter(s -> !s.getPolicyId().isBlank()).findAny().isEmpty()) {
			return 0;
		}

		String addressValue = g.get(0).getSourceAddress() + " " + g.stream()
				.filter(s -> !s.getPolicyId().isBlank())
				.map(s -> (s.getValue() + " " + formatCurrency(s.getPolicyId(), s.getAssetName())).trim())
				.collect(Collectors.joining("+"));

		return cardanoCli.calculateMinUtxo(addressValue);
	}

	private String formatCurrency(String policyId, String assetName) {
		if (StringUtils.isBlank(assetName)) {
			return policyId;
		} else {
			return policyId + "." + Hex.encodeHexString(assetName.getBytes(StandardCharsets.UTF_8));
		}
	}

}
