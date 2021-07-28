package de.peterspace.cardanotools.dbsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import de.peterspace.cardanotools.TrackExecutionTime;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.PolicyScanner;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardanoDbSyncClient {

	private final TokenRegistry tokenRegistry;
	private final PolicyScanner policyScanner;

	private static final String getTxInputQuery = "select distinct address from tx_out "
			+ "inner join tx_in on tx_out.tx_id = tx_in.tx_out_id "
			+ "inner join tx on tx.id = tx_in.tx_in_id and tx_in.tx_out_index = tx_out.index "
			+ "where tx.hash = ? ;";

	private static final String getAddressFundingQuery = "select to2.address \r\n"
			+ "from utxo_byron_view uv \r\n"
			+ "join tx t on t.id = uv.tx_id\r\n"
			+ "join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index \r\n"
			+ "where uv.address = ? and to2.address != ?";

	private static final String tokenQuery = "select\r\n"
			+ "encode(mtm.policy::bytea, 'hex') policyId,\r\n"
			+ "encode(mtm.name::bytea, 'escape') tokenName,\r\n"
			+ "mtm.quantity,\r\n"
			+ "encode(t.hash ::bytea, 'hex') txId,\r\n"
			+ "tm.json,\r\n"
			+ "t.invalid_before,\r\n"
			+ "t.invalid_hereafter,\r\n"
			+ "b.block_no,\r\n"
			+ "b.epoch_no,\r\n"
			+ "b.epoch_slot_no, \r\n"
			+ "t.id tid, \r\n"
			+ "mtm.id mintid,\r\n"
			+ "b.slot_no\r\n"
			+ "from ma_tx_mint mtm\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id ";

	private static final String walletTokenQuery = "with  \r\n"
			+ "stake_address_id as (\r\n"
			+ "	select to2.stake_address_id\r\n"
			+ "	from tx_out to2 \r\n"
			+ "	where \r\n"
			+ "	to2.address = ?\r\n"
			+ "	union ALL\r\n"
			+ "	select sa.id \r\n"
			+ "	from stake_address sa \r\n"
			+ "	where \r\n"
			+ "	sa.\"view\" = ?\r\n"
			+ "	limit 1\r\n"
			+ "),\r\n"
			+ "owned_tokens as (\r\n"
			+ "	SELECT mto.policy \"policy\", mto.name \"name\", quantity quantity, to2.id txId\r\n"
			+ "	FROM utxo_byron_view uv \r\n"
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" \r\n"
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id \r\n"
			+ "	where uv.stake_address_id = (select * from stake_address_id)\r\n"
			+ ")\r\n"
			+ "select\r\n"
			+ "encode(ot.policy::bytea, 'hex') policyId,\r\n"
			+ "encode(ot.name::bytea, 'escape') tokenName,\r\n"
			+ "max(ot.quantity) quantity,\r\n"
			+ "max(encode(t.hash ::bytea, 'hex')) txId,\r\n"
			+ "jsonb_agg(tm.json)->0 json,\r\n"
			+ "max(t.invalid_before) invalid_before,\r\n"
			+ "max(t.invalid_hereafter) invalid_hereafter,\r\n"
			+ "max(b.block_no) block_no,\r\n"
			+ "max(b.epoch_no) epoch_no,\r\n"
			+ "max(b.epoch_slot_no) epoch_slot_no, \r\n"
			+ "max(t.id) tid, \r\n"
			+ "max(mtm.id) mintid, \r\n"
			+ "max(b.slot_no) \r\n"
			+ "from owned_tokens ot\r\n"
			+ "join ma_tx_mint mtm on mtm.\"policy\"=ot.policy and mtm.\"name\"=ot.name\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id\r\n"
			+ "group by ot.policy, ot.name\r\n"
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.policy = ot.policy and sorter.name = ot.name) desc";

	private static final String delegatorsQuery = "with \r\n"
			+ "delegates as (\r\n"
			+ "	select stake_address, pool_address, stake_address_id  from (\r\n"
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, sa.\"view\" stake_address, ph.\"view\" pool_address, d.addr_id stake_address_id\r\n"
			+ "		from delegation d \r\n"
			+ "		join pool_hash ph on ph.id = d.pool_hash_id\r\n"
			+ "		join stake_address sa on sa.id = d.addr_id \r\n"
			+ "		join delegation alldelegations on alldelegations.addr_id=d.addr_id\r\n"
			+ "		where \r\n"
			+ "		ph.view='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u'\r\n"
			+ "	) inner_query\r\n"
			+ "	where row_number=1 and pool_address='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u'\r\n"
			+ "),\r\n"
			+ "delegator_addresses as (\r\n"
			+ "	select row_number() over(PARTITION BY txo.stake_address_id order by txo.stake_address_id, txo.tx_id desc) row_number, encode(tx.hash, 'hex') tx_hash_view,  stake_address, txo.address , pool_address, txo.stake_address_id, txo.value from delegates\r\n"
			+ "	join tx_out txo on txo.stake_address_id = delegates.stake_address_id\r\n"
			+ "	join tx on tx.id =txo.tx_id\r\n"
			+ ")\r\n"
			+ "select stake_address,address,(select sum(value) from utxo_byron_view uv where uv.stake_address_id = delegator_addresses.stake_address_id) pledge from delegator_addresses where row_number = 1";

	@Value("${cardano-db-sync.url}")
	String url;

	@Value("${cardano-db-sync.username}")
	String username;

	@Value("${cardano-db-sync.password}")
	String password;

	private HikariDataSource hds;

	@PostConstruct
	public void init() throws SQLException {
		hds = new HikariDataSource();
		hds.setInitializationFailTimeout(60000l);
		hds.setJdbcUrl(url);
		hds.setUsername(username);
		hds.setPassword(password);
		hds.setMaximumPoolSize(30);
		hds.setAutoCommit(false);

		try (Connection connection = hds.getConnection()) {
			log.debug("Create json index");
			connection.createStatement().executeUpdate("CREATE INDEX if not exists jsonmetadata_fts ON tx_metadata USING gin (( to_tsvector('english',json) ));");
			connection.createStatement().executeUpdate("CREATE INDEX if not exists tokenname_fts ON ma_tx_mint USING gin (( to_tsvector('english',encode(name::bytea, 'escape')) ));");
		}

	}

	@PreDestroy
	public void shutdown() {
		hds.close();
	}

	public List<String> getInpuAddresses(List<String> txids) {
		return txids.stream().flatMap(txid -> getInpuAddresses(txid).stream()).collect(Collectors.toList());
	}

	@TrackExecutionTime
	public long getBalance(String address) {
		List<String> addresses = new ArrayList<>();
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getBalance = connection.prepareStatement("select sum(value) from utxo_byron_view uv where uv.address = ?");
			getBalance.setString(1, address);
			ResultSet result = getBalance.executeQuery();
			while (result.next()) {
				return result.getLong(1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

	@TrackExecutionTime
	public List<String> getInpuAddresses(String txid) {
		List<String> addresses = new ArrayList<>();
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(getTxInputQuery);
			byte[] bytes = Hex.decodeHex(txid);
			getTxInput.setBytes(1, bytes);
			ResultSet result = getTxInput.executeQuery();
			while (result.next()) {
				addresses.add(result.getString(1));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return addresses;
	}

	@TrackExecutionTime
	public List<String> getFundingAddresses(String address) {
		List<String> addresses = new ArrayList<>();
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(getAddressFundingQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			ResultSet result = getTxInput.executeQuery();
			while (result.next()) {
				addresses.add(result.getString(1));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return addresses;
	}

	@TrackExecutionTime
	@Cacheable("findTokens")
	public List<TokenData> findTokens(String string, Long fromMintid) throws DecoderException {

		byte[] bytes = null;

		try {
			bytes = Hex.decodeHex(string);
		} catch (DecoderException e) {
		}

		try (Connection connection = hds.getConnection()) {

			String findTokenQuery = "SELECT * FROM (";
			findTokenQuery += tokenQuery;
			findTokenQuery += "WHERE ";
			findTokenQuery += "to_tsvector('english',encode(mtm.name::bytea, 'escape')) @@ to_tsquery(?) ";

			findTokenQuery += "UNION ";
			findTokenQuery += tokenQuery;
			findTokenQuery += "WHERE ";
			findTokenQuery += "encode(mtm.name::bytea, 'escape') ilike concat('%', ? ,'%') ";

			findTokenQuery += "UNION ";
			findTokenQuery += tokenQuery;
			findTokenQuery += "WHERE ";
			findTokenQuery += "to_tsvector('english',json) @@ to_tsquery(?) ";

			if (bytes != null) {
				findTokenQuery += "UNION ";
				findTokenQuery += tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "mtm.policy=? ";

				findTokenQuery += "UNION ";
				findTokenQuery += tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "t.hash=? ";
			}
			findTokenQuery += ") AS U ";

			if (fromMintid != null)
				findTokenQuery += "WHERE mintid > ? ";

			findTokenQuery += "order by mintid ";
			findTokenQuery += "limit 100 ";

			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);

			getTxInput.setString(1, string.trim().replace(" ", " | "));
			getTxInput.setString(2, string.trim().replace(" ", " | "));
			getTxInput.setString(3, string.trim().replace(" ", " | "));

			if (bytes != null) {
				getTxInput.setBytes(4, bytes);
				getTxInput.setBytes(5, bytes);
				if (fromMintid != null)
					getTxInput.setLong(6, fromMintid);
			} else {
				if (fromMintid != null)
					getTxInput.setLong(4, fromMintid);
			}

			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	@Cacheable("latestTokens")
	public List<TokenData> latestTokens(Long fromMintid) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			String findTokenQuery = tokenQuery;
			if (fromMintid != null)
				findTokenQuery += "WHERE mtm.id < ? ";
			findTokenQuery += "order by mtm.id desc ";
			findTokenQuery += "limit 100 ";
			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);
			if (fromMintid != null)
				getTxInput.setLong(1, fromMintid);
			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	@Cacheable("walletTokens")
	public List<TokenData> walletTokens(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			String findTokenQuery = walletTokenQuery;
			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<TokenData> parseTokenResultset(ResultSet result) throws SQLException {
		List<TokenData> tokenDatas = new ArrayList<>();
		while (result.next()) {
			TokenData tokenData = new TokenData();
			tokenData.setPolicyId(result.getString(1));
			tokenData.setName(result.getString(2));
			tokenData.setQuantity(result.getLong(3));
			tokenData.setTxId(result.getString(4));
			tokenData.setJson(result.getString(5));
			tokenData.setInvalid_before(result.getLong(6));
			if (result.wasNull()) {
				tokenData.setInvalid_before(null);
			}
			tokenData.setInvalid_hereafter(result.getLong(7));
			if (result.wasNull()) {
				tokenData.setInvalid_hereafter(null);
			}
			tokenData.setBlockNo(result.getLong(8));
			tokenData.setEpochNo(result.getLong(9));
			tokenData.setEpochSlotNo(result.getLong(10));
			tokenData.setTid(result.getLong(11));
			tokenData.setMintid(result.getLong(12));
			tokenData.setSlotNo(result.getLong(13));
			tokenDatas.add(tokenData);

			String subject = CardanoUtil.createSubject(tokenData.getPolicyId(), tokenData.getName());
			tokenData.setTokenRegistryMetadata(tokenRegistry.getTokenRegistryMetadata().get(subject));

			tokenData.setPolicy(policyScanner.getPolicies().get(tokenData.getPolicyId()));
		}

		return tokenDatas;
	}

}
