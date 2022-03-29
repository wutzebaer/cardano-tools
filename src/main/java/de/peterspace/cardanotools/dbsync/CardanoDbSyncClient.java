package de.peterspace.cardanotools.dbsync;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import de.peterspace.cardanotools.TrackExecutionTime;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.PolicyScanner;
import de.peterspace.cardanotools.cardano.ProjectRegistry;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.StakePosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardanoDbSyncClient {

	@Value("${pool.address}")
	private String poolAddress;

	private final TokenRegistry tokenRegistry;
	private final PolicyScanner policyScanner;
	private final ProjectRegistry projectRegistry;
	private final TaskExecutor taskExecutor;

	private static final String getTxInputQuery = "select distinct address from tx_out "
			+ "inner join tx_in on tx_out.tx_id = tx_in.tx_out_id "
			+ "inner join tx on tx.id = tx_in.tx_in_id and tx_in.tx_out_index = tx_out.index "
			+ "where tx.hash = ? ;";

	private static final String getAddressFundingQueryHistory = "select to2.address\r\n"
			+ "from tx_out to1\r\n"
			+ "join tx t on t.id = to1.tx_id\r\n"
			+ "join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index\r\n"
			+ "where to1.address = ?\r\n"
			+ "and to2.address != ?";

	private static final String getAddressFundingQuery = "select to2.address \r\n"
			+ "from utxo_view uv \r\n"
			+ "join tx t on t.id = uv.tx_id\r\n"
			+ "join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index \r\n"
			+ "where uv.address = ? and to2.address != ?";

	private static final String offerFundingQuery = "select max(address), sum(value) from \r\n"
			+ "	(select max(to2.stake_address_id) stake_address_id, max(to2.address) address, max(uv.value) \"value\"\r\n"
			+ "	from utxo_view uv \r\n"
			+ "	join tx_in ti on ti.tx_in_id = uv.tx_id\r\n"
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index \r\n"
			+ "	where uv.address = ?\r\n"
			+ "	group by uv.tx_id, uv.\"index\") sub\r\n"
			+ "group by stake_address_id";

	private static final String offerTokenFundingQuery = "select max(address), max(\"policy\"), max(\"name\"), sum(quantity) from \r\n"
			+ "	(select max(to3.stake_address_id) stake_address_id, max(to3.address) address, max(encode(mto.policy::bytea, 'hex')) \"policy\", max(encode(mto.name::bytea, 'escape')) \"name\", max(quantity) quantity\r\n"
			+ "	from utxo_view uv \r\n"
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" \r\n"
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id \r\n"
			+ "	join tx_in ti on ti.tx_in_id = uv.tx_id\r\n"
			+ "	join tx_out to3 on to3.tx_id = ti.tx_out_id and to3.\"index\" = ti.tx_out_index\r\n"
			+ "	where uv.address = ?\r\n"
			+ "	group by uv.tx_id, uv.\"index\", \"policy\", \"name\") sub\r\n"
			+ "group by stake_address_id, \"policy\", \"name\"";

	private static final String tokenQuery = "select\r\n"
			+ "encode(ma.policy::bytea, 'hex') policyId,\r\n"
			+ "ma.name tokenName,\r\n"
			+ "mtm.quantity,\r\n"
			+ "encode(t.hash ::bytea, 'hex') txId,\r\n"
			+ "tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape') json,\r\n"
			+ "t.invalid_before,\r\n"
			+ "t.invalid_hereafter,\r\n"
			+ "b.block_no,\r\n"
			+ "b.epoch_no,\r\n"
			+ "b.epoch_slot_no, \r\n"
			+ "t.id tid, \r\n"
			+ "mtm.id mintid,\r\n"
			+ "b.slot_no,\r\n"
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident = mtm.ident) total_supply\r\n"
			+ "from ma_tx_mint mtm\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id \r\n"
			+ "join multi_asset ma on ma.id = mtm.ident ";

	private static final String offerTokenQuery = "with\r\n"
			+ "addresses as (\r\n"
			+ "	select to2.address\r\n"
			+ "	from tx_out to1\r\n"
			+ "	join tx t on t.id = to1.tx_id\r\n"
			+ "	join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index\r\n"
			+ "	where to1.address = ?\r\n"
			+ "	and to2.address != ?\r\n"
			+ "),\r\n"
			+ "stake_address_id as (\r\n"
			+ "	select to2.stake_address_id\r\n"
			+ "	from tx_out to2 \r\n"
			+ "	where \r\n"
			+ "	to2.address in (select address from addresses)\r\n"
			+ "),\r\n"
			+ "owned_tokens as (\r\n"
			+ "	SELECT mto.policy \"policy\", mto.name \"name\", quantity quantity, to2.id txId\r\n"
			+ "	FROM utxo_view uv \r\n"
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" \r\n"
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id \r\n"
			+ "	where uv.stake_address_id in (select distinct stake_address_id from stake_address_id)\r\n"
			+ ")\r\n"
			+ "select\r\n"
			+ "encode(ot.policy::bytea, 'hex') policyId,\r\n"
			+ "ot.name tokenName,\r\n"
			+ "max(ot.quantity) quantity,\r\n"
			+ "max(encode(t.hash ::bytea, 'hex')) txId,\r\n"
			+ "jsonb_agg(tm.json->encode(mtm.policy::bytea, 'hex')->encode(mtm.name::bytea, 'escape'))->-1 json,\r\n"
			+ "max(t.invalid_before) invalid_before,\r\n"
			+ "max(t.invalid_hereafter) invalid_hereafter,\r\n"
			+ "max(b.block_no) block_no,\r\n"
			+ "max(b.epoch_no) epoch_no,\r\n"
			+ "max(b.epoch_slot_no) epoch_slot_no, \r\n"
			+ "max(t.id) tid, \r\n"
			+ "max(mtm.id) mintid, \r\n"
			+ "max(b.slot_no), \r\n"
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.\"policy\"=ot.\"policy\" and mtm2.\"name\"=ot.\"name\") total_supply \r\n"
			+ "from owned_tokens ot\r\n"
			+ "join ma_tx_mint mtm on mtm.\"policy\"=ot.policy and mtm.\"name\"=ot.name\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id\r\n"
			+ "group by ot.policy, ot.name\r\n"
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.policy = ot.policy and sorter.name = ot.name) desc";

	private static final String addressTokenQuery = "with  \r\n"
			+ "owned_tokens as (\r\n"
			+ "	SELECT mto.ident ident, sum(quantity) quantity\r\n"
			+ "	FROM utxo_view uv \r\n"
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" \r\n"
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id \r\n"
			+ "	where uv.address = ?\r\n"
			+ "	group by mto.ident\r\n"
			+ ")\r\n"
			+ "select\r\n"
			+ "encode(ma.policy::bytea, 'hex') policyId,\r\n"
			+ "ma.name tokenName,\r\n"
			+ "max(ot.quantity) quantity,\r\n"
			+ "max(encode(t.hash ::bytea, 'hex')) txId,\r\n"
			+ "jsonb_agg(tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape'))->-1 json,\r\n"
			+ "max(t.invalid_before) invalid_before,\r\n"
			+ "max(t.invalid_hereafter) invalid_hereafter,\r\n"
			+ "max(b.block_no) block_no,\r\n"
			+ "max(b.epoch_no) epoch_no,\r\n"
			+ "max(b.epoch_slot_no) epoch_slot_no, \r\n"
			+ "max(t.id) tid, \r\n"
			+ "max(mtm.id) mintid, \r\n"
			+ "max(b.slot_no), \r\n"
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident=ma.id) total_supply \r\n"
			+ "from owned_tokens ot\r\n"
			+ "join ma_tx_mint mtm on mtm.ident = ot.ident\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id\r\n"
			+ "join multi_asset ma on ma.id = ot.ident\r\n"
			+ "group by ma.id\r\n"
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.ident=ma.id) desc";

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
			+ "	SELECT mto.ident ident, sum(quantity) quantity\r\n"
			+ "	FROM utxo_view uv \r\n"
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" \r\n"
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id \r\n"
			+ "	where uv.stake_address_id = (select * from stake_address_id)\r\n"
			+ "	group by mto.ident\r\n"
			+ ")\r\n"
			+ "select\r\n"
			+ "encode(ma.policy::bytea, 'hex') policyId,\r\n"
			+ "ma.name tokenName,\r\n"
			+ "max(ot.quantity) quantity,\r\n"
			+ "max(encode(t.hash ::bytea, 'hex')) txId,\r\n"
			+ "jsonb_agg(tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape'))->-1 json,\r\n"
			+ "max(t.invalid_before) invalid_before,\r\n"
			+ "max(t.invalid_hereafter) invalid_hereafter,\r\n"
			+ "max(b.block_no) block_no,\r\n"
			+ "max(b.epoch_no) epoch_no,\r\n"
			+ "max(b.epoch_slot_no) epoch_slot_no, \r\n"
			+ "max(t.id) tid, \r\n"
			+ "max(mtm.id) mintid, \r\n"
			+ "max(b.slot_no), \r\n"
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident=ma.id) total_supply \r\n"
			+ "from owned_tokens ot\r\n"
			+ "join ma_tx_mint mtm on mtm.ident = ot.ident\r\n"
			+ "join tx t on t.id = mtm.tx_id \r\n"
			+ "left join tx_metadata tm on tm.tx_id = t.id \r\n"
			+ "join block b on b.id = t.block_id\r\n"
			+ "join multi_asset ma on ma.id = ot.ident\r\n"
			+ "group by ma.id\r\n"
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.ident=ma.id) desc";

	private static final String currentDelegateQuery = "with \r\n"
			+ "potential_delegates as (\r\n"
			+ "	select to2.stake_address_id\r\n"
			+ "	from tx_out to1\r\n"
			+ "	join tx t on t.id = to1.tx_id\r\n"
			+ "	join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index\r\n"
			+ "	where to1.address = ?\r\n"
			+ "	and to2.address != ?\r\n"
			+ ")\r\n"
			+ ",delegates as (\r\n"
			+ "	select stake_address_id from (\r\n"
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.\"view\" pool_address, d.addr_id stake_address_id\r\n"
			+ "		from delegation d \r\n"
			+ "		join pool_hash ph on ph.id = d.pool_hash_id\r\n"
			+ "		join stake_address sa on sa.id = d.addr_id \r\n"
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id\r\n"
			+ "	) inner_query\r\n"
			+ "	where row_number=1 and pool_address=?\r\n"
			+ ")\r\n"
			+ ",stakeamounts as (\r\n"
			+ "	select \r\n"
			+ "	(select view from stake_address sa where sa.id=utxo.stake_address_id),\r\n"
			+ "	sum(value)\r\n"
			+ "	from utxo_view utxo \r\n"
			+ "	join delegates d on d.stake_address_id = utxo.stake_address_id\r\n"
			+ "	group by utxo.stake_address_id\r\n"
			+ ")\r\n"
			+ "select coalesce(sum(sum),0) from stakeamounts";

	private static final String allDelegateQuery = "with \r\n"
			+ "potential_delegates as (\r\n"
			+ "	select to2.stake_address_id\r\n"
			+ "	from tx_out to1\r\n"
			+ "	join tx t on t.id = to1.tx_id\r\n"
			+ "	join tx_in ti on ti.tx_in_id = t.id\r\n"
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index\r\n"
			+ "	where to1.address = ?\r\n"
			+ "	and to2.address != ?\r\n"
			+ ")\r\n"
			+ ",delegates as (\r\n"
			+ "	select stake_address_id, pool_id from (\r\n"
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.id pool_id, d.addr_id stake_address_id\r\n"
			+ "		from delegation d \r\n"
			+ "		join pool_hash ph on ph.id = d.pool_hash_id\r\n"
			+ "		join stake_address sa on sa.id = d.addr_id \r\n"
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id\r\n"
			+ "	) inner_query\r\n"
			+ "	where row_number=1 \r\n"
			+ ")\r\n"
			+ ",stakeamounts as (\r\n"
			+ "	select \r\n"
			+ "	(select view from stake_address sa where sa.id=utxo.stake_address_id),\r\n"
			+ "	max(d.pool_id) pool_id,\r\n"
			+ "	sum(value)\r\n"
			+ "	from utxo_view utxo \r\n"
			+ "	join delegates d on d.stake_address_id = utxo.stake_address_id\r\n"
			+ "	group by utxo.stake_address_id\r\n"
			+ ")\r\n"
			+ "select \r\n"
			+ "coalesce(sum(sum),0) funds,\r\n"
			+ "(select view from pool_hash ph where ph.id=sa.pool_id order by id desc limit 1) pool_hash,\r\n"
			+ "(select ticker_name from pool_offline_data pod where pod.pool_id=sa.pool_id order by id desc limit 1) ticker_name,\r\n"
			+ "(select sum(amount) from epoch_stake es where es.pool_id=sa.pool_id group by es.epoch_no order by es.epoch_no desc limit 1) total_stake\r\n"
			+ "from stakeamounts sa\r\n"
			+ "group by (sa.view, sa.pool_id)";

	private static final String delegatorsQuery = "with \r\n"
			+ "potential_delegates as (\r\n"
			+ "	select d.addr_id stake_address_id\r\n"
			+ "	from delegation d \r\n"
			+ "	join pool_hash ph on ph.id = d.pool_hash_id\r\n"
			+ "	where \r\n"
			+ "	ph.view='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u'\r\n"
			+ ")\r\n"
			+ ",delegates as (\r\n"
			+ "	select stake_address_id from (\r\n"
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.\"view\" pool_address, d.addr_id stake_address_id\r\n"
			+ "		from delegation d \r\n"
			+ "		join pool_hash ph on ph.id = d.pool_hash_id\r\n"
			+ "		join stake_address sa on sa.id = d.addr_id \r\n"
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id\r\n"
			+ "	) inner_query\r\n"
			+ "	where row_number=1 and pool_address='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u'\r\n"
			+ ")\r\n"
			+ "select \r\n"
			+ "(select view from stake_address sa where sa.id=utxo.stake_address_id),\r\n"
			+ "sum(value)\r\n"
			+ "from utxo_view utxo \r\n"
			+ "join delegates d on d.stake_address_id = utxo.stake_address_id\r\n"
			+ "group by utxo.stake_address_id";

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

		taskExecutor.execute(() -> {
			try (Connection connection = hds.getConnection()) {
				log.debug("Create json index");
				connection.createStatement().execute("CREATE INDEX if not exists jsonmetadata_fts ON tx_metadata USING gin (( to_tsvector('english',json) ));");
				connection.commit();
				log.debug("Create json index finshed");
			} catch (SQLException e) {
				log.error("Create json index", e);
			}
		});
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
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getBalance = connection.prepareStatement("select sum(value) from utxo_view uv where uv.address = ?");
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
	public List<String> getFundingAddressesHistory(String address) {
		List<String> addresses = new ArrayList<>();
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(getAddressFundingQueryHistory);
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
	public List<TokenData> findTokens(String string, Long fromMintid) throws DecoderException {

		try (Connection connection = hds.getConnection()) {

			String findTokenQuery = "SELECT * FROM (\r\n";
			findTokenQuery += "SELECT U.*, row_number() over(PARTITION by  policyId, tokenName order by mintid desc) rn FROM (\r\n";

			Map<Integer, Object> fillPlaceholders = new HashMap<>();

			String[] bits = string.split("\\.");
			if (bits.length == 2 && bits[0].length() == 56) {
				findTokenQuery += CardanoDbSyncClient.tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "ma.policy=? AND ma.name=? ";

				fillPlaceholders.put(1, Hex.decodeHex(bits[0]));
				fillPlaceholders.put(2, bits[1].getBytes(StandardCharsets.UTF_8));
				if (fromMintid != null)
					fillPlaceholders.put(3, fromMintid);

			} else if (bits.length == 1 && bits[0].length() == 56) {
				findTokenQuery += CardanoDbSyncClient.tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "ma.policy=?";

				fillPlaceholders.put(1, Hex.decodeHex(bits[0]));
				if (fromMintid != null)
					fillPlaceholders.put(2, fromMintid);

			} else {
				findTokenQuery += CardanoDbSyncClient.tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "to_tsvector('english',json) @@ to_tsquery(?) ";
				findTokenQuery += "and to_tsvector('english',tm.json->encode(ma.policy::bytea, 'hex')->convert_from(ma.name, 'UTF8')) @@ to_tsquery(?) ";

				String tsquery = string.trim().replaceAll("[^A-Za-z0-9]+", " & ");
				fillPlaceholders.put(1, tsquery);
				fillPlaceholders.put(2, tsquery);
				if (fromMintid != null)
					fillPlaceholders.put(3, fromMintid);
			}

			findTokenQuery += ") AS U where U.quantity > 0 ";
			findTokenQuery += ") as numbered ";

			findTokenQuery += "where rn = 1 ";
			if (fromMintid != null)
				findTokenQuery += "and mintid > ? ";

			findTokenQuery += "order by epoch_no, tokenname ";
			findTokenQuery += "limit 100 ";

			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);
			for (Entry<Integer, Object> entry : fillPlaceholders.entrySet()) {
				getTxInput.setObject(entry.getKey(), entry.getValue());
			}

			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<TokenData> policyTokens(String policyId) throws DecoderException {

		try (Connection connection = hds.getConnection()) {

			String findTokenQuery = "SELECT * FROM (\r\n";
			findTokenQuery += "SELECT U.*, row_number() over(PARTITION by  policyId, tokenName order by mintid desc) rn FROM (\r\n";

			findTokenQuery += CardanoDbSyncClient.tokenQuery;
			findTokenQuery += "WHERE ";
			findTokenQuery += "encode(ma.policy::bytea, 'hex')=?";
			findTokenQuery += ") AS U where U.quantity > 0 ";
			findTokenQuery += ") as numbered ";
			findTokenQuery += "where rn = 1 ";
			findTokenQuery += "order by epoch_no, tokenname ";

			PreparedStatement getTxInput = connection.prepareStatement(findTokenQuery);
			getTxInput.setObject(1, policyId);

			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<TokenData> latestTokens(Long fromMintid) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			String findTokenQuery = tokenQuery;

			if (fromMintid == null) {
				// no where
			} else if (fromMintid > 0) {
				findTokenQuery += "WHERE mtm.id < ? ";
			} else {
				fromMintid = -fromMintid;
				findTokenQuery += "WHERE mtm.id > ? ";
			}

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
	public List<TokenData> getOfferableTokens(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			String findTokenQuery = offerTokenQuery;
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

	@TrackExecutionTime
	public long getCurrentStake(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(currentDelegateQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			getTxInput.setString(3, poolAddress);
			ResultSet result = getTxInput.executeQuery();
			result.next();
			return result.getLong(1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<StakePosition> allStakes(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(allDelegateQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			ResultSet result = getTxInput.executeQuery();
			return parseStakePositionResultset(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<TokenData> walletTokens(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(walletTokenQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<TokenData> addressTokens(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(addressTokenQuery);
			getTxInput.setString(1, address);
			ResultSet result = getTxInput.executeQuery();
			List<TokenData> tokenDatas = parseTokenResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<OfferFunding> getOfferFundings(String offerAddress) {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(offerFundingQuery);
			getTxInput.setString(1, offerAddress);
			ResultSet result = getTxInput.executeQuery();
			List<OfferFunding> offerFundings = new ArrayList<OfferFunding>();
			while (result.next()) {
				offerFundings.add(new OfferFunding(result.getString(1), result.getLong(2)));
			}
			return offerFundings;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public List<OfferTokenFunding> getOfferTokenFundings(String offerAddress) {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(offerTokenFundingQuery);
			getTxInput.setString(1, offerAddress);
			ResultSet result = getTxInput.executeQuery();
			List<OfferTokenFunding> offerFundings = new ArrayList<OfferTokenFunding>();
			while (result.next()) {
				offerFundings.add(new OfferTokenFunding(result.getString(1), result.getString(2), result.getString(3), result.getLong(4)));
			}
			return offerFundings;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<StakePosition> parseStakePositionResultset(ResultSet result) throws SQLException {
		List<StakePosition> stakePositions = new ArrayList<>();
		while (result.next()) {
			StakePosition stakePosition = new StakePosition();
			stakePosition.setFunds(result.getLong(1));
			stakePosition.setPoolHash(result.getString(2));
			stakePosition.setTickerName(result.getString(3));
			stakePosition.setTotalStake(result.getLong(4));
			stakePositions.add(stakePosition);
		}

		return stakePositions;
	}

	private List<TokenData> parseTokenResultset(ResultSet result) throws SQLException {
		List<TokenData> tokenDatas = new ArrayList<>();
		while (result.next()) {
			TokenData tokenData = new TokenData();
			tokenData.setPolicyId(result.getString(1));
			tokenData.setName(new String(result.getBytes(2), StandardCharsets.UTF_8));
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
			tokenData.setTotalSupply(result.getLong(14));
			tokenData.setProjectMetadata(projectRegistry.getProjectRegistryMetadata().get(tokenData.getPolicyId()));
			tokenDatas.add(tokenData);

			String subject = CardanoUtil.createSubject(tokenData.getPolicyId(), tokenData.getName());
			tokenData.setTokenRegistryMetadata(tokenRegistry.getTokenRegistryMetadata().get(subject));

			tokenData.setPolicy(policyScanner.getPolicies().get(tokenData.getPolicyId()));
		}

		return tokenDatas;
	}

}
