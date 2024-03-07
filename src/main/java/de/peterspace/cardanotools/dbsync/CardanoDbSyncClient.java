package de.peterspace.cardanotools.dbsync;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;
import de.peterspace.cardanodbsyncapi.client.model.AccountStatementRow;
import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import de.peterspace.cardanodbsyncapi.client.model.TxOut;
import de.peterspace.cardanodbsyncapi.client.model.Utxo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CardanoDbSyncClient {

	@Value("${pool.address}")
	private String poolAddress;

	@Value("${cardano-db-sync.api}")
	String apiBasePath;

	private RestHandlerApi restHandlerApi;

	@PostConstruct
	public void init() throws SQLException {
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(apiBasePath);
		restHandlerApi = new RestHandlerApi(apiClient);
	}

	public List<Utxo> getUtxos(String address) {
		return Collections.unmodifiableList(restHandlerApi.getUtxos(address));
	}

	public String getReturnAddress(String stakeAddress) {
		return restHandlerApi.getReturnAddress(stakeAddress).getAddress();
	}

	public String getStakeAddress(String address) {
		return restHandlerApi.getStakeAddress(address).getAddress();
	}

	public List<EpochStake> getEpochStake(String poolHash, int epoch) {
		return Collections.unmodifiableList(restHandlerApi.getEpochStake(poolHash, epoch));
	}

	public List<AccountStatementRow> getStatement(String address) {
		return Collections.unmodifiableList(restHandlerApi.getStatement(address));
	}

	public long getTip() {
		return restHandlerApi.getTip();
	}

	public boolean isTransactionConfirmed(String txId) {
		return restHandlerApi.isTransactionConfirmed(txId);
	}

	public String getTransactionMetadata(String txId) {
		return restHandlerApi.getTransactionMetadata(txId);
	}

	public List<TxOut> getTransactionOutputs(String txId) {
		return restHandlerApi.getTransactionOutputs(txId);
	}

}
