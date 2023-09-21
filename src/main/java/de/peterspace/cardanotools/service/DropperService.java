package de.peterspace.cardanotools.service;

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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import de.peterspace.cardanotools.cardano.CardanoCli;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.TransactionOutputs;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Address;
import de.peterspace.cardanotools.model.Drop;
import de.peterspace.cardanotools.model.DropNft;
import de.peterspace.cardanotools.model.Wallet;
import de.peterspace.cardanotools.repository.DropRepository;
import de.peterspace.cardanotools.repository.WalletRepository;
import jakarta.transaction.Transactional;
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

	private final Cache<String, Boolean> temporaryBlacklist = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
	private final Set<Utxo> permanentBlacklist = new HashSet<>();

	public List<String> findFundedAddresses() {
		List<Drop> drops = dropRepository.findAll();
		return drops
				.stream()
				.map(d -> d.getAddress().getAddress())
				.filter(fundAddress -> {
					Map<String, List<Utxo>> transactionInputGroups = findUtxosGroupedBySourceWallet(fundAddress);
					return transactionInputGroups.size() > 0;
				})
				.collect(Collectors.toList());
	}

	@Scheduled(cron = "0 * * * * *")
	@Transactional
	public void dropNfts() {

		List<Drop> drops = dropRepository.findAll();

		log.info("START Dropper cycle for {} drops", drops.size());

		drops.parallelStream().forEach(drop -> {
			Address fundAddress = drop.getAddress();
			Map<String, List<Utxo>> transactionInputGroups = findUtxosGroupedBySourceWallet(fundAddress.getAddress());

			for (List<Utxo> utxos : transactionInputGroups.values()) {
				String stakeAddress = utxos.get(0).getSourceAddress();
				try {

					List<String> whitelist = drop.getWhitelist();
					long lockedFunds = calculateLockedFunds(utxos);

					if (!whitelist.isEmpty() && !whitelist.contains(stakeAddress)) {
						refund(fundAddress, utxos, lockedFunds, "Not in whitelist");
					} else if (!drop.isRunning()) {
						refund(fundAddress, utxos, lockedFunds, "Not running");
					} else {

						Optional<Wallet> wallet = Optional.empty();
						if (!whitelist.isEmpty()) {
							wallet = walletRepository.findByDropAndStakeAddress(drop, stakeAddress);
							if (wallet.isEmpty()) {
								wallet = Optional.of(new Wallet(null, drop, stakeAddress, 0));
							}
						}

						long price = drop.getPrice();
						int mintsLeft = drop.getMaxPerTransaction() - wallet.map(w -> w.getTokensMinted()).orElse(0);

						if (drop.getDropNftsAvailableAssetNames().size() == 0) {
							refund(fundAddress, utxos, lockedFunds, "No tokens left");
						} else if (mintsLeft < 1) {
							refund(fundAddress, utxos, lockedFunds, "No tokens left for your wallet");
						} else if (drop.getPolicy().getPolicyDueSlot() < CardanoUtil.currentSlot()) {
							refund(fundAddress, utxos, lockedFunds, "Policy has locked");
						} else if (hasEnoughFunds(price, utxos, lockedFunds)) {
							long funds = calculateAvailableFunds(utxos) - lockedFunds;
							int amount = (int) NumberUtils.min(funds / price, mintsLeft);
							List<DropNft> tokens = findTokens(drop, amount);
							long totalPrice = tokens.size() * price;

							if (wallet.isPresent()) {
								wallet.get().setTokensMinted(wallet.get().getTokensMinted() + tokens.size());
								walletRepository.save(wallet.get());
								log.info("Wallet {} has {} tokens left", wallet.get().getStakeAddress(), drop.getMaxPerTransaction() - wallet.get().getTokensMinted());
							}

							List<String> usedAssets = tokens.stream().map(t -> t.getAssetName()).collect(Collectors.toList());
							drop.getDropNftsAvailableAssetNames().removeAll(usedAssets);
							drop.getDropNftsSoldAssetNames().addAll(usedAssets);
							dropRepository.save(drop);

							sell(drop, utxos, tokens, totalPrice, lockedFunds);
						} else {
							refund(fundAddress, utxos, lockedFunds, "Noth enough funds");
						}

					}

				} catch (Exception e) {
					log.error("Utxo failed to process", e);
				} finally {
					temporaryBlacklist.put(stakeAddress, true);
				}
			}
		});

		log.info("END  Dropper cycle");
	}

	private Map<String, List<Utxo>> findUtxosGroupedBySourceWallet(String address) {
		List<Utxo> offerFundings = cardanoDbSyncClient.getUtxos(address);
		Map<String, List<Utxo>> transactionInputGroups = offerFundings.stream()
				.filter(of -> temporaryBlacklist.getIfPresent(of.getSourceAddress()) == null)
				.filter(of -> !permanentBlacklist.contains(of))
				.collect(Collectors.groupingBy(of -> of.getSourceAddress(), LinkedHashMap::new, Collectors.toList()));
		return transactionInputGroups;
	}

	private void sell(Drop drop, List<Utxo> utxos, List<DropNft> tokens, long totalPrice, long lockedFunds) throws Exception {

		taskExecutor.execute(() -> {
			try {
				String buyerAddress = cardanoDbSyncClient.getReturnAddress(utxos.get(0).getSourceAddress());

				log.info("selling {} tokens to {} : {}", tokens.size(), buyerAddress, tokens);

				TransactionOutputs transactionOutputs = new TransactionOutputs();

				// send tokens
				for (DropNft token : tokens) {
					transactionOutputs.add(buyerAddress, formatCurrency(drop.getPolicy().getPolicyId(), Hex.encodeHexString(token.getAssetName().getBytes())), 1);
				}

				// return input tokens to seller
				utxos.stream().filter(e -> e.getMaPolicyId() != null).forEach(i -> {
					transactionOutputs.add(buyerAddress, formatCurrency(i.getMaPolicyId(), i.getMaName()), i.getValue());
				});

				// min output for tokens
				transactionOutputs.add(buyerAddress, "", cardanoCli.calculateMinUtxo(transactionOutputs.toCliFormat(buyerAddress)));

				// return change to buyer
				long change = calculateAvailableFunds(utxos) - lockedFunds - totalPrice;
				transactionOutputs.add(buyerAddress, "", change);

				if (true) {
					transactionOutputs.add(pledgeAddress, "", drop.getFee());
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
				String txId = cardanoCli.mint(utxos, transactionOutputs, metaData, drop.getAddress(), drop.getPolicy(), drop.getProfitAddress());
				permanentBlacklist.addAll(utxos);
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

	private void refund(Address fundAddress, List<Utxo> Utxo, long lockedFunds, String reason) throws Exception {

		taskExecutor.execute(() -> {
			try {
				// determine amount of tokens
				String buyerAddress = Utxo.get(0).getSourceAddress();

				TransactionOutputs transactionOutputs = new TransactionOutputs();

				Utxo.stream().filter(e -> e.getMaPolicyId() != null).forEach(i -> {
					transactionOutputs.add(buyerAddress, formatCurrency(i.getMaPolicyId(), i.getMaName()), i.getValue());
				});
				transactionOutputs.add(buyerAddress, "", lockedFunds);

				JSONObject message = new JSONObject().put("674", new JSONObject().put("msg", new JSONArray().put(reason)));
				String txId = cardanoCli.mint(Utxo, transactionOutputs, message, fundAddress, null, buyerAddress);
				permanentBlacklist.addAll(Utxo);
				log.info("Successfully refunded, txid: {} Reason: {}", txId, reason);

			} catch (Exception e) {
				log.error("Refund failed (Reason " + reason + ")", e);
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

	private boolean hasEnoughFunds(long minFunds, List<Utxo> g, long lockedFunds) throws Exception {
		return calculateAvailableFunds(g) - lockedFunds >= (minFunds);
	}

	private long calculateAvailableFunds(List<Utxo> Utxo) {
		return Utxo.stream().filter(e -> e.getMaPolicyId() == null).mapToLong(e -> e.getValue()).sum();
	}

	private long calculateLockedFunds(List<Utxo> g) throws Exception {
		if (g.stream().allMatch(s -> s.getMaPolicyId() == null)) {
			return 0;
		}

		String addressValue = g.get(0).getSourceAddress() + " " + g.stream()
				.filter(s -> s.getMaPolicyId() != null)
				.map(s -> (s.getValue() + " " + formatCurrency(s.getMaPolicyId(), s.getMaName())).trim())
				.collect(Collectors.joining("+"));

		return cardanoCli.calculateMinUtxo(addressValue);
	}

	private String formatCurrency(String policyId, String assetNameHex) {
		if (StringUtils.isBlank(assetNameHex)) {
			return policyId;
		} else {
			return policyId + "." + assetNameHex;
		}
	}

}
