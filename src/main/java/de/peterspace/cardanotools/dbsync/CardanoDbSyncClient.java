package de.peterspace.cardanotools.dbsync;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import com.zaxxer.hikari.HikariDataSource;

import de.peterspace.cardanotools.TrackExecutionTime;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CardanoDbSyncClient {

	private static final String getTxInputQuery = "select distinct address from tx_out "
			+ "inner join tx_in on tx_out.tx_id = tx_in.tx_out_id "
			+ "inner join tx on tx.id = tx_in.tx_in_id and tx_in.tx_out_index = tx_out.index "
			+ "where tx.hash = ? ;";

	private static final String getAddressFundingQuery = "select distinct tx_out.address \r\n"
			+ "	from tx_out\r\n"
			+ "	inner join tx_in on tx_out.tx_id = tx_in.tx_out_id\r\n"
			+ "	inner join tx on tx.id = tx_in.tx_in_id and tx_in.tx_out_index = tx_out.index\r\n"
			+ "	inner join tx_out tx_out2 on tx_out2.tx_id = tx.id\r\n"
			+ "	where tx_out2.address = ? and tx_out.address != ?";

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
			+ "t.id tid \r\n"
			+ "from ma_tx_mint mtm\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id ";

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
	public List<TokenData> findTokens(String string) throws DecoderException {

		byte[] bytes = null;

		try {
			bytes = Hex.decodeHex(string);
		} catch (DecoderException e) {
		}

		try (Connection connection = hds.getConnection()) {

			String findTokenQuery = tokenQuery;
			findTokenQuery += "WHERE ";
			findTokenQuery += "to_tsvector('english',encode(mtm.name::bytea, 'escape')) @@ to_tsquery(?) ";

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
			findTokenQuery += "order by tid, tokenName  ";
			findTokenQuery += "limit 10 ";

			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);

			getTxInput.setString(1, string.trim().replace(" ", " | "));
			getTxInput.setString(2, string.trim().replace(" ", " | "));

			if (bytes != null) {
				getTxInput.setBytes(3, bytes);
				getTxInput.setBytes(4, bytes);
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
	public List<TokenData> latestTokens(Long fromTid) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			String findTokenQuery = tokenQuery;
			if (fromTid != null)
				findTokenQuery += "WHERE t.id < ?";
			findTokenQuery += "order by t.id desc ";
			findTokenQuery += "limit 25 ";
			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);
			if (fromTid != null)
				getTxInput.setLong(1, fromTid);
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
			tokenData.setBlock_no(result.getLong(8));
			tokenData.setEpoch_no(result.getLong(9));
			tokenData.setEpoch_slot_no(result.getLong(10));
			tokenData.setTid(result.getLong(11));
			tokenDatas.add(tokenData);
		}

		return tokenDatas;
	}

}
