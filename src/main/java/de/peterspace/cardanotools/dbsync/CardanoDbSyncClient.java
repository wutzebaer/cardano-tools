package de.peterspace.cardanotools.dbsync;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;
import de.peterspace.cardanodbsyncapi.client.model.AccountStatementRow;
import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import de.peterspace.cardanodbsyncapi.client.model.PoolInfo;
import de.peterspace.cardanodbsyncapi.client.model.TokenDetails;
import de.peterspace.cardanodbsyncapi.client.model.TokenListItem;
import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import de.peterspace.cardanotools.TrackExecutionTime;
import de.peterspace.cardanotools.cardano.ProjectRegistry;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.service.PriceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CardanoDbSyncClient {

	@Value("${pool.address}")
	private String poolAddress;

	private final TokenRegistry tokenRegistry;
	private final ProjectRegistry projectRegistry;
	private final TaskExecutor taskExecutor;
	private final PriceService priceService;

	@Value("${cardano-db-sync.api}")
	String apiBasePath;

	private RestHandlerApi restHandlerApi;

	@PostConstruct
	public void init() throws SQLException {
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(apiBasePath);
		restHandlerApi = new RestHandlerApi(apiClient);
	}

	@TrackExecutionTime
	@Cacheable("getUtxos")
	public List<Utxo> getUtxos(String address) {
		return Collections.unmodifiableList(restHandlerApi.getUtxos(address));
	}

	@TrackExecutionTime
	@Cacheable("getReturnAddress")
	public String getReturnAddress(String address) {
		return restHandlerApi.getReturnAddress(address).getAddress();
	}

	@TrackExecutionTime
	@Cacheable("getPoolList")
	public List<PoolInfo> getPoolList() {
		return Collections.unmodifiableList(restHandlerApi.getPoolList());
	}

	@TrackExecutionTime
	@Cacheable("getEpochStake")
	public List<EpochStake> getEpochStake(String poolHash, int epoch) {
		return Collections.unmodifiableList(restHandlerApi.getEpochStake(poolHash, epoch));
	}

	@TrackExecutionTime
	@Cacheable("getTokenList")
	public List<TokenListItem> getTokenList(Long afterMintid, Long beforeMintid, String filter) {
		return Collections.unmodifiableList(restHandlerApi.getTokenList(afterMintid, beforeMintid, filter));
	}

	@TrackExecutionTime
	@Cacheable("getTokenDetails")
	public TokenDetails getTokenDetails(String policyId, String assetName) {
		return restHandlerApi.getTokenDetails(policyId, assetName);
	}

	@TrackExecutionTime
	@Cacheable("getStatement")
	public List<AccountStatementRow> getStatement(String address) {
		return Collections.unmodifiableList(restHandlerApi.getStatement(address));
	}

}
