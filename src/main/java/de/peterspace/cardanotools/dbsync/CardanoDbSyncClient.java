package de.peterspace.cardanotools.dbsync;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;
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
import lombok.extern.slf4j.Slf4j;

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
	public List<Utxo> getUtxos(String address) {
		return restHandlerApi.getUtxos(address);
	}

	@TrackExecutionTime
	public String getReturnAddress(String address) {
		return restHandlerApi.getReturnAddress(address).getAddress();
	}

	@TrackExecutionTime
	public List<PoolInfo> getPoolList() {
		return restHandlerApi.getPoolList();
	}

	@TrackExecutionTime
	public List<EpochStake> getEpochStake(String poolHash, int epoch) {
		return restHandlerApi.getEpochStake(poolHash, epoch);
	}

	@TrackExecutionTime
	public List<TokenListItem> getTokenList(Long afterMintid, Long beforeMintid, String filter) {
		return restHandlerApi.getTokenList(afterMintid, beforeMintid, filter);
	}

	@TrackExecutionTime
	public TokenDetails getTokenDetails(String policyId, String assetName) {
		return restHandlerApi.getTokenDetails(policyId, assetName);
	}

}
