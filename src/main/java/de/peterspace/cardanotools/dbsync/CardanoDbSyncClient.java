package de.peterspace.cardanotools.dbsync;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import de.peterspace.cardanotools.TrackExecutionTime;
import de.peterspace.cardanotools.cardano.CardanoNode;
import de.peterspace.cardanotools.cardano.CardanoUtil;
import de.peterspace.cardanotools.cardano.ProjectRegistry;
import de.peterspace.cardanotools.cardano.TokenRegistry;
import de.peterspace.cardanotools.model.EpochStakePosition;
import de.peterspace.cardanotools.model.StakePosition;
import de.peterspace.cardanotools.model.TransactionInputs;
import de.peterspace.cardanotools.rest.dto.AccountStatementRow;
import de.peterspace.cardanotools.rest.dto.SnapshotRequest;
import de.peterspace.cardanotools.rest.dto.SnapshotRequest.SnapshotRequestPolicy;
import de.peterspace.cardanotools.rest.dto.SnapshotResult;
import de.peterspace.cardanotools.rest.dto.SnapshotResult.SnapshotResultRow;
import de.peterspace.cardanotools.rest.dto.SnapshotResult.SnapshotResultRow.SnapshotResultToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardanoDbSyncClient {

	final private CardanoNode cardanoNode;

	@Value("${pool.address}")
	private String poolAddress;

	private final TokenRegistry tokenRegistry;
	private final ProjectRegistry projectRegistry;
	private final TaskExecutor taskExecutor;

	private static final String getTxInputQuery = "select distinct address from tx_out "
			+ "inner join tx_in on tx_out.tx_id = tx_in.tx_out_id "
			+ "inner join tx on tx.id = tx_in.tx_in_id and tx_in.tx_out_index = tx_out.index "
			+ "where tx.hash = ? ;";

	private static final String getAddressFundingQueryHistory = "select to2.address "
			+ "from tx_out to1 "
			+ "join tx t on t.id = to1.tx_id "
			+ "join tx_in ti on ti.tx_in_id = t.id "
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "where to1.address = ? "
			+ "and to2.address != ?";

	private static final String getAddressFundingQuery = "select to2.address "
			+ "from utxo_view uv "
			+ "join tx t on t.id = uv.tx_id "
			+ "join tx_in ti on ti.tx_in_id = t.id "
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "where uv.address = ? and to2.address != ?";

	private static final String offerFundingQuery = "select max(address), sum(value) from "
			+ "	(select max(to2.stake_address_id) stake_address_id, max(to2.address) address, max(uv.value) \"value\" "
			+ "	from utxo_view uv "
			+ "	join tx_in ti on ti.tx_in_id = uv.tx_id "
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "	where uv.address = ? "
			+ "	group by uv.tx_id, uv.\"index\") sub "
			+ "group by stake_address_id";

	private static final String offerTokenFundingQuery = "select max(address), max(\"policy\"), max(\"name\"), sum(quantity) from "
			+ "	(select max(to3.stake_address_id) stake_address_id, max(to3.address) address, max(encode(mto.policy::bytea, 'hex')) \"policy\", max(encode(mto.name::bytea, 'escape')) \"name\", max(quantity) quantity "
			+ "	from utxo_view uv "
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" "
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id "
			+ "	join tx_in ti on ti.tx_in_id = uv.tx_id "
			+ "	join tx_out to3 on to3.tx_id = ti.tx_out_id and to3.\"index\" = ti.tx_out_index "
			+ "	where uv.address = ? "
			+ "	group by uv.tx_id, uv.\"index\", \"policy\", \"name\") sub "
			+ "group by stake_address_id, \"policy\", \"name\"";

	private static final String tokenQuery = "select "
			+ "encode(ma.policy::bytea, 'hex') policyId, "
			+ "ma.name tokenName, "
			+ "mtm.quantity, "
			+ "encode(t.hash ::bytea, 'hex') txId, "
			+ "tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape') json, "
			+ "t.invalid_before, "
			+ "t.invalid_hereafter, "
			+ "b.block_no, "
			+ "b.epoch_no, "
			+ "b.epoch_slot_no, "
			+ "t.id tid, "
			+ "mtm.id mintid, "
			+ "b.slot_no, "
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident = mtm.ident) total_supply, "
			+ "ma.fingerprint, "
			+ "jsonb_pretty(s2.json) \"policy\" "
			+ "from ma_tx_mint mtm "
			+ "join tx t on t.id = mtm.tx_id "
			+ "left join tx_metadata tm on tm.tx_id = t.id and tm.key=721 "
			+ "join block b on b.id = t.block_id "
			+ "join multi_asset ma on ma.id = mtm.ident "
			+ "join script s2 on s2.hash=ma.\"policy\" ";

	private static final String offerTokenQuery = "with "
			+ "addresses as ( "
			+ "	select to2.address "
			+ "	from tx_out to1 "
			+ "	join tx t on t.id = to1.tx_id "
			+ "	join tx_in ti on ti.tx_in_id = t.id "
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "	where to1.address = ? "
			+ "	and to2.address != ? "
			+ "), "
			+ "stake_address_id as ( "
			+ "	select to2.stake_address_id "
			+ "	from tx_out to2 "
			+ "	where "
			+ "	to2.address in (select address from addresses) "
			+ "), "
			+ "owned_tokens as ( "
			+ "	SELECT mto.policy \"policy\", mto.name \"name\", quantity quantity, to2.id txId "
			+ "	FROM utxo_view uv "
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" "
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id "
			+ "	where uv.stake_address_id in (select distinct stake_address_id from stake_address_id) "
			+ ") "
			+ "select "
			+ "encode(ot.policy::bytea, 'hex') policyId, "
			+ "ot.name tokenName, "
			+ "max(ot.quantity) quantity, "
			+ "max(encode(t.hash ::bytea, 'hex')) txId, "
			+ "jsonb_agg(tm.json->encode(mtm.policy::bytea, 'hex')->encode(mtm.name::bytea, 'escape'))->-1 json, "
			+ "max(t.invalid_before) invalid_before, "
			+ "max(t.invalid_hereafter) invalid_hereafter, "
			+ "max(b.block_no) block_no, "
			+ "max(b.epoch_no) epoch_no, "
			+ "max(b.epoch_slot_no) epoch_slot_no, "
			+ "max(t.id) tid, "
			+ "max(mtm.id) mintid, "
			+ "max(b.slot_no), "
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.\"policy\"=ot.\"policy\" and mtm2.\"name\"=ot.\"name\") total_supply "
			+ "from owned_tokens ot "
			+ "join ma_tx_mint mtm on mtm.\"policy\"=ot.policy and mtm.\"name\"=ot.name "
			+ "join tx t on t.id = mtm.tx_id "
			+ "left join tx_metadata tm on tm.tx_id = t.id and tm.key=721 "
			+ "join block b on b.id = t.block_id "
			+ "group by ot.policy, ot.name "
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.policy = ot.policy and sorter.name = ot.name) desc";

	private static final String addressTokenQuery = "with "
			+ "owned_tokens as ( "
			+ "	SELECT mto.ident ident, sum(quantity) quantity "
			+ "	FROM utxo_view uv "
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" "
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id "
			+ "	where uv.address = ? "
			+ "	group by mto.ident "
			+ ") "
			+ "select "
			+ "encode(ma.policy::bytea, 'hex') policyId, "
			+ "ma.name tokenName, "
			+ "max(ot.quantity) quantity, "
			+ "max(encode(t.hash ::bytea, 'hex')) txId, "
			+ "max((tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape'))::text) json, "
			+ "max(t.invalid_before) invalid_before, "
			+ "max(t.invalid_hereafter) invalid_hereafter, "
			+ "max(b.block_no) block_no, "
			+ "max(b.epoch_no) epoch_no, "
			+ "max(b.epoch_slot_no) epoch_slot_no, "
			+ "max(t.id) tid, "
			+ "max(mtm.id) mintid, "
			+ "max(b.slot_no), "
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident=ma.id) total_supply, "
			+ "ma.fingerprint, "
			+ "max(jsonb_pretty(s2.json)) \"policy\" "
			+ "from owned_tokens ot "
			+ "join ma_tx_mint mtm on mtm.ident = ot.ident "
			+ "join tx t on t.id = mtm.tx_id "
			+ "left join tx_metadata tm on tm.tx_id = t.id and tm.key=721 "
			+ "join block b on b.id = t.block_id "
			+ "join multi_asset ma on ma.id = ot.ident "
			+ "join script s2 on s2.hash=ma.\"policy\" "
			+ "group by ma.id "
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.ident=ma.id) desc";

	private static final String walletTokenQuery = "with "
			+ "stake_address_id as ( "
			+ "	select to2.stake_address_id "
			+ "	from tx_out to2 "
			+ "	where "
			+ "	to2.address = ? "
			+ "	union ALL "
			+ "	select sa.id "
			+ "	from stake_address sa "
			+ "	where "
			+ "	sa.\"view\" = ? "
			+ "	limit 1 "
			+ "), "
			+ "owned_tokens as ( "
			+ "	SELECT mto.ident ident, sum(quantity) quantity "
			+ "	FROM utxo_view uv "
			+ "	join tx_out to2 on to2.tx_id = uv.tx_id and to2.\"index\" = uv.\"index\" "
			+ "	join ma_tx_out mto on mto.tx_out_id = to2.id "
			+ "	where uv.stake_address_id = (select * from stake_address_id) "
			+ "	group by mto.ident "
			+ ") "
			+ "select "
			+ "encode(ma.policy::bytea, 'hex') policyId, "
			+ "ma.name tokenName, "
			+ "max(ot.quantity) quantity, "
			+ "max(encode(t.hash ::bytea, 'hex')) txId, "
			+ "max((tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape'))::text) json, "
			+ "max(t.invalid_before) invalid_before, "
			+ "max(t.invalid_hereafter) invalid_hereafter, "
			+ "max(b.block_no) block_no, "
			+ "max(b.epoch_no) epoch_no, "
			+ "max(b.epoch_slot_no) epoch_slot_no, "
			+ "max(t.id) tid, "
			+ "max(mtm.id) mintid, "
			+ "max(b.slot_no), "
			+ "(select sum(quantity) from ma_tx_mint mtm2 where mtm2.ident=ma.id) total_supply, "
			+ "ma.fingerprint, "
			+ "max(jsonb_pretty(s2.json)) \"policy\" "
			+ "from owned_tokens ot "
			+ "join ma_tx_mint mtm on mtm.ident = ot.ident "
			+ "join tx t on t.id = mtm.tx_id "
			+ "left join tx_metadata tm on tm.tx_id = t.id and tm.key=721 "
			+ "join block b on b.id = t.block_id "
			+ "join multi_asset ma on ma.id = ot.ident "
			+ "join script s2 on s2.hash=ma.policy "
			+ "group by ma.id "
			+ "order by (select min(id) from ma_tx_mint sorter where sorter.ident=ma.id) desc";

	private static final String findStakeAddressIds = "select to2.stake_address_id "
			+ "from tx_out to2 "
			+ "where "
			+ "to2.address = ANY (?) "
			+ "union "
			+ "select sa.id "
			+ "from stake_address sa "
			+ "where "
			+ "sa.\"view\" = ANY (?)";

	private static final String currentDelegateQuery = "with "
			+ "potential_delegates as ( "
			+ "	select to2.stake_address_id "
			+ "	from tx_out to1 "
			+ "	join tx t on t.id = to1.tx_id "
			+ "	join tx_in ti on ti.tx_in_id = t.id "
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "	where to1.address = ? "
			+ "	and to2.address != ? "
			+ ") "
			+ ",delegates as ( "
			+ "	select stake_address_id from ( "
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.\"view\" pool_address, d.addr_id stake_address_id "
			+ "		from delegation d "
			+ "		join pool_hash ph on ph.id = d.pool_hash_id "
			+ "		join stake_address sa on sa.id = d.addr_id "
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id "
			+ "	) inner_query "
			+ "	where row_number=1 and pool_address=? "
			+ ") "
			+ ",stakeamounts as ( "
			+ "	select "
			+ "	(select view from stake_address sa where sa.id=utxo.stake_address_id), "
			+ "	sum(value) "
			+ "	from utxo_view utxo "
			+ "	join delegates d on d.stake_address_id = utxo.stake_address_id "
			+ "	group by utxo.stake_address_id "
			+ ") "
			+ "select coalesce(sum(sum),0) from stakeamounts";

	private static final String allDelegateQuery = "with "
			+ "potential_delegates as ( "
			+ "	select to2.stake_address_id "
			+ "	from tx_out to1 "
			+ "	join tx t on t.id = to1.tx_id "
			+ "	join tx_in ti on ti.tx_in_id = t.id "
			+ "	join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "	where to1.address = ? "
			+ "	and to2.address != ? "
			+ ") "
			+ ",delegates as ( "
			+ "	select stake_address_id, pool_id from ( "
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.id pool_id, d.addr_id stake_address_id "
			+ "		from delegation d "
			+ "		join pool_hash ph on ph.id = d.pool_hash_id "
			+ "		join stake_address sa on sa.id = d.addr_id "
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id "
			+ "	) inner_query "
			+ "	where row_number=1 "
			+ ") "
			+ ",stakeamounts as ( "
			+ "	select "
			+ "	(select view from stake_address sa where sa.id=utxo.stake_address_id), "
			+ "	max(d.pool_id) pool_id, "
			+ "	sum(value) "
			+ "	from utxo_view utxo "
			+ "	join delegates d on d.stake_address_id = utxo.stake_address_id "
			+ "	group by utxo.stake_address_id "
			+ ") "
			+ "select "
			+ "coalesce(sum(sum),0) funds, "
			+ "(select view from pool_hash ph where ph.id=sa.pool_id order by id desc limit 1) pool_hash, "
			+ "(select ticker_name from pool_offline_data pod where pod.pool_id=sa.pool_id order by id desc limit 1) ticker_name, "
			+ "(select sum(amount) from epoch_stake es where es.pool_id=sa.pool_id group by es.epoch_no order by es.epoch_no desc limit 1) total_stake "
			+ "from stakeamounts sa "
			+ "group by (sa.view, sa.pool_id)";

	private static final String delegatorsQuery = "with "
			+ "potential_delegates as ( "
			+ "	select d.addr_id stake_address_id "
			+ "	from delegation d "
			+ "	join pool_hash ph on ph.id = d.pool_hash_id "
			+ "	where "
			+ "	ph.view='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u' "
			+ ") "
			+ ",delegates as ( "
			+ "	select stake_address_id from ( "
			+ "		select row_number() over(PARTITION BY d.addr_id order by d.addr_id, d.id desc) row_number, ph.\"view\" pool_address, d.addr_id stake_address_id "
			+ "		from delegation d "
			+ "		join pool_hash ph on ph.id = d.pool_hash_id "
			+ "		join stake_address sa on sa.id = d.addr_id "
			+ "		join potential_delegates on potential_delegates.stake_address_id=d.addr_id "
			+ "	) inner_query "
			+ "	where row_number=1 and pool_address='pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u' "
			+ ") "
			+ "select "
			+ "(select view from stake_address sa where sa.id=utxo.stake_address_id), "
			+ "sum(value) "
			+ "from utxo_view utxo "
			+ "join delegates d on d.stake_address_id = utxo.stake_address_id "
			+ "group by utxo.stake_address_id";

	private static final String epochStakeQuery = "select distinct "
			+ "sa.\"view\" stake_address, "
			+ "(select to2.address from tx_out to2 where to2.stake_address_id=es.addr_id limit 1), "
			+ "es.amount "
			+ "from pool_hash ph "
			+ "join epoch_stake es on es.pool_id=ph.id "
			+ "join stake_address sa on sa.id=es.addr_id "
			+ "left join pool_owner po on po.pool_hash_id=ph.id and po.addr_id=sa.id "
			+ "where "
			+ "ph.view=? "
			+ "and epoch_no=?";

	private static final String epochStakeQuery_vasil = "select distinct "
			+ "sa.\"view\" stake_address, "
			+ "(select to2.address from tx_out to2 where to2.stake_address_id=es.addr_id limit 1), "
			+ "es.amount "
			+ "from pool_hash ph "
			+ "join epoch_stake es on es.pool_id=ph.id "
			+ "join stake_address sa on sa.id=es.addr_id "
			+ "left join pool_update pu on pu.hash_id = ph.id "
			+ "left join pool_owner po on po.pool_update_id =pu.id and po.addr_id=sa.id "
			+ "where "
			+ "ph.view=? "
			+ "and epoch_no=?";

	private static final String poolListQuery = "select distinct pod.ticker_name, ph.\"view\" "
			+ "from pool_offline_data pod "
			+ "join pool_hash ph on ph.id=pod.pool_id "
			+ "order by pod.ticker_name";

	private static final String utxoQuery = "select uv.id uvid, max(encode(t2.hash::bytea, 'hex')) txhash, max(uv.\"index\") txix, max(uv.value) \"value\", max(to2.stake_address_id) stake_address, max(to2.address) source_address, '' policyId, '' assetName, null metadata "
			+ "from utxo_view uv "
			+ "join tx t2 on t2.id = uv.tx_id "
			+ "join tx_in ti on ti.tx_in_id = uv.tx_id "
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "join tx_out to3 on to3.tx_id = uv.tx_id and to3.\"index\" = uv.\"index\" "
			+ "left join ma_tx_out mto on mto.tx_out_id=to3.id "
			+ "where "
			+ "uv.address = ? "
			+ "group by uv.id "
			+ "union "
			+ "select "
			+ "uv.id uvid, "
			+ "max(encode(t2.hash::bytea, 'hex')) txhash, "
			+ "max(uv.\"index\") txix, "
			+ "max(mto.quantity) \"value\", "
			+ "max(to2.stake_address_id) stake_address, "
			+ "max(to2.address) source_address, "
			+ "encode(ma.\"policy\" ::bytea, 'hex') policyId, "
			+ "convert_from(ma.name, 'UTF8') assetName, "
			+ "(select json->encode(ma.\"policy\" ::bytea, 'hex')->convert_from(ma.name, 'UTF8') from tx_metadata tm where tm.tx_id = (select max(tx_id) from ma_tx_mint mtm where mtm.ident=ma.id) and key=721) metadata "
			+ "from utxo_view uv "
			+ "join tx t2 on t2.id = uv.tx_id "
			+ "join tx_in ti on ti.tx_in_id = uv.tx_id "
			+ "join tx_out to2 on to2.tx_id = ti.tx_out_id and to2.\"index\" = ti.tx_out_index "
			+ "join tx_out to3 on to3.tx_id = uv.tx_id and to3.\"index\" = uv.\"index\" "
			+ "join ma_tx_out mto on mto.tx_out_id=to3.id "
			+ "join multi_asset ma on ma.id=mto.ident "
			+ "where "
			+ "uv.address = ? "
			+ "group by uv.id, ma.id "
			+ "order by uvid, policyId, assetName";

	private static final String tokenAmountByAddress = "SELECT "
			+ "   utxo_view.address, "
			+ "   sum(ma_tx_out.quantity) as qty "
			+ "   from utxo_view "
			+ "   inner join tx_out on utxo_view.tx_id = tx_out.tx_id and tx_out.index = utxo_view.index "
			+ "   inner join ma_tx_out on ma_tx_out.tx_out_id = tx_out.id "
			+ "   inner join multi_asset on ma_tx_out.ident = multi_asset.id "
			+ "   where multi_asset.policy = decode(?, 'hex') "
			+ "group by utxo_view.address "
			+ "order by  sum(ma_tx_out.quantity) DESC; "
			+ "";

	private static final String accountStatementQuery = """
				select
					min("time") "timestamp",
					min("epoch_no") epoch,
					min(encode(hash, 'hex')) tx_hash,
					sum("WITHDRAWN") withdrawn,
					sum("REWARDS") rewards,
					sum("OUT") "OUT",
					sum("IN") "IN",
					(sum("IN")-sum("OUT")-sum("WITHDRAWN")+sum("REWARDS")) "change",
					sum(sum("IN")-sum("OUT")-sum("WITHDRAWN")+sum("REWARDS")) over (order by txId asc rows between unbounded preceding and current row),
					string_agg(distinct "TYPE", ',') operations
					from (
							select
								t2.id txId,
								b2.time,
								b2.epoch_no,
								t2.hash,
								'IN' "TYPE",
								0 "OUT",
								to2.value "IN",
								0 "WITHDRAWN",
								0 "REWARDS"
							from tx_out to2
							join tx t2 on t2.id=to2.tx_id
							join block b2 on b2.id=t2.block_id
							join stake_address sa on sa.id=to2.stake_address_id
							where sa."view" = ?
							union all
							select
								t2.id txId,
								b2.time,
								b2.epoch_no,
								t2.hash,
								'OUT' "TYPE",
								to2.value "OUT",
								0 "IN",
								0 "WITHDRAWN",
								0 "REWARDS"
							from tx_in ti
							join tx t2 on t2.id=ti.tx_in_id
							join tx_out to2 on to2.tx_id=ti.tx_out_id and to2."index"=ti.tx_out_index
							join block b2 on b2.id=t2.block_id
							join stake_address sa on sa.id=to2.stake_address_id
							where sa."view" = ?
							union all
							select
								t2.id txId,
								b2.time,
								b2.epoch_no,
								t2.hash,
								'WITHDRAW' "TYPE",
								0 "OUT",
								0 "IN",
								wi.amount "WITHDRAWN",
								0 "REWARDS"
							from withdrawal wi
							join tx t2 on t2.id=wi.tx_id
							join block b2 on b2.id=t2.block_id
							join stake_address sa on sa.id=wi.addr_id
							where sa."view" = ?
							union all
							select
								(select min(t2.id) from block bl join tx t2 on t2.block_id=bl.id where bl.epoch_no=rw.earned_epoch) "txId",
								(select min("time") from block bl where bl.epoch_no=rw.earned_epoch) "time",
								rw.earned_epoch epoch_no,
								null hash,
								'REWARD_'||rw."type" "TYPE",
								0 "OUT",
								0 "IN",
								0 "WITHDRAWN",
								rw.amount "REWARDS"
							from reward rw
							join stake_address sa on sa.id=rw.addr_id
							where sa."view" = ?
				) movings
				group by txId
				order by txId desc
			""";

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

			String findTokenQuery = "SELECT * FROM ( ";
			findTokenQuery += "SELECT U.*, row_number() over(PARTITION by  policyId, tokenName order by mintid desc) rn FROM ( ";

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

			} else if (string.length() == 44 && string.startsWith("asset")) {
				System.err.println("");
				findTokenQuery += CardanoDbSyncClient.tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "ma.fingerprint=?";
				fillPlaceholders.put(1, string);
				if (fromMintid != null)
					fillPlaceholders.put(2, fromMintid);
			} else {
				findTokenQuery += CardanoDbSyncClient.tokenQuery;
				findTokenQuery += "WHERE ";
				findTokenQuery += "to_tsvector('english',tm.json) @@ to_tsquery(?) ";
				findTokenQuery += "and to_tsvector('english',tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name, 'escape')) @@ to_tsquery(?) ";

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

			// findTokenQuery += "order by policyId, regexp_replace(encode(tokenname,
			// 'escape'), '[0-9]+', '', 'g'), regexp_replace('0' || encode(tokenname,
			// 'escape'), '[^0-9]+', '', 'g')::bigint ";
			findTokenQuery += "order by mintid ";
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

			String findTokenQuery = "SELECT * FROM ( ";
			findTokenQuery += "SELECT U.*, row_number() over(PARTITION by  policyId, tokenName order by mintid desc) rn FROM ( ";

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
			String findTokenQuery = tokenQuery + " WHERE ";

			if (fromMintid == null) {
				findTokenQuery += "true ";
			} else if (fromMintid > 0) {
				findTokenQuery += "mtm.id < ? ";
			} else {
				fromMintid = -fromMintid;
				findTokenQuery += "mtm.id > ? ";
			}

			findTokenQuery += "AND tm.json->encode(ma.policy::bytea, 'hex')->encode(ma.name::bytea, 'escape') IS NOT NULL ";

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
	public List<EpochStakePosition> epochStake(String pool, int epoch) throws DecoderException {
		try (Connection connection = hds.getConnection()) {

			String query = epochStakeQuery_vasil;

			PreparedStatement getTxInput = connection.prepareStatement(query);
			getTxInput.setString(1, pool);
			getTxInput.setInt(2, epoch);
			ResultSet result = getTxInput.executeQuery();
			return parseEpochStakePositionResultset(result);
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
	public List<AccountStatementRow> accountStatement(String address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(accountStatementQuery);
			getTxInput.setString(1, address);
			getTxInput.setString(2, address);
			getTxInput.setString(3, address);
			getTxInput.setString(4, address);
			ResultSet result = getTxInput.executeQuery();
			List<AccountStatementRow> tokenDatas = parseAccountStatementResultset(result);
			return tokenDatas;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TrackExecutionTime
	public Set<Long> findStakeAddressIds(String[] address) throws DecoderException {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(findStakeAddressIds);
			Array addressArray = connection.createArrayOf("VARCHAR", address);
			getTxInput.setArray(1, addressArray);
			getTxInput.setArray(2, addressArray);
			ResultSet result = getTxInput.executeQuery();

			Set<Long> stakeAddressIds = new HashSet<>();
			while (result.next()) {
				stakeAddressIds.add(result.getLong(1));
			}
			return stakeAddressIds;

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

	@TrackExecutionTime
	public List<PoolInfo> getPoolList() {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(poolListQuery);
			ResultSet result = getTxInput.executeQuery();
			List<PoolInfo> polInfos = new ArrayList<>();
			while (result.next()) {
				polInfos.add(new PoolInfo(result.getString(1), result.getString(2)));
			}
			return polInfos;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<AccountStatementRow> parseAccountStatementResultset(ResultSet result) throws SQLException {
		List<AccountStatementRow> stakePositions = new ArrayList<>();
		while (result.next()) {
			stakePositions.add(new AccountStatementRow(result.getTimestamp("timestamp"),
					result.getInt("epoch"),
					result.getString("tx_hash"),
					result.getLong("withdrawn"),
					result.getLong("rewards"),
					result.getLong("OUT"),
					result.getLong("IN"),
					result.getLong("change"),
					result.getLong("sum"),
					result.getString("operations").split(",")));
		}

		return stakePositions;
	}

	private List<EpochStakePosition> parseEpochStakePositionResultset(ResultSet result) throws SQLException {
		List<EpochStakePosition> stakePositions = new ArrayList<>();
		while (result.next()) {
			EpochStakePosition stakePosition = new EpochStakePosition();
			stakePosition.setStakeAddress(result.getString(1));
			stakePosition.setAddress(result.getString(2));
			stakePosition.setAmount(result.getLong(3));
			stakePositions.add(stakePosition);
		}

		return stakePositions;
	}

	private List<StakePosition> parseStakePositionResultset(ResultSet result) throws SQLException {
		List<StakePosition> stakePositions = new ArrayList<>();
		while (result.next()) {
			StakePosition stakePosition = new StakePosition();
			stakePosition.setFunds(result.getLong(1));
			stakePosition.setPoolHash(result.getString(2));
			stakePosition.setTickerName(Optional.ofNullable(result.getString(3)).orElse("UNKNOWN"));
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
			tokenData.setFingerprint(result.getString(15));
			tokenData.setProjectMetadata(projectRegistry.getProjectRegistryMetadata().get(tokenData.getPolicyId()));
			tokenDatas.add(tokenData);

			String subject = CardanoUtil.createSubject(tokenData.getPolicyId(), tokenData.getName());
			tokenData.setTokenRegistryMetadata(tokenRegistry.getTokenRegistryMetadata().get(subject));

			tokenData.setPolicy(result.getString(16));
		}

		return tokenDatas;
	}

	public List<TransactionInputs> getAddressUtxos(String offerAddress) {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(utxoQuery);
			getTxInput.setString(1, offerAddress);
			getTxInput.setString(2, offerAddress);
			ResultSet result = getTxInput.executeQuery();
			List<TransactionInputs> offerFundings = new ArrayList<TransactionInputs>();
			while (result.next()) {
				offerFundings.add(new TransactionInputs(result.getString(2), result.getInt(3), result.getLong(4), result.getLong(5), result.getString(6), result.getString(7), result.getString(8), result.getString(9)));
			}
			return offerFundings;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@lombok.Value
	public static class TokenAmountByAddress {
		private String address;
		private long qty;
	}

	@Cacheable("getTokenAmountByAddress")
	public List<TokenAmountByAddress> getTokenAmountByAddress(String policyId) {
		try (Connection connection = hds.getConnection()) {
			PreparedStatement getTxInput = connection.prepareStatement(tokenAmountByAddress);
			getTxInput.setString(1, policyId);
			ResultSet result = getTxInput.executeQuery();
			List<TokenAmountByAddress> tokenAmountByAddress = new ArrayList<>();
			while (result.next()) {
				tokenAmountByAddress.add(new TokenAmountByAddress(result.getString(1), result.getLong(2)));
			}
			return tokenAmountByAddress;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SnapshotResult createSnapshot(SnapshotRequest snapshotRequest) {
		String query = ""
				+ "with "
				+ "max_block_id as ( "
				+ "	select max(b.id) max_block_id "
				+ "	from block b "
				+ "	where b.\"time\"<? "
				+ "), "
				+ "max_tx_id as ( "
				+ "	select max(tx.id) max_tx_id "
				+ "	from tx tx "
				+ "	where tx.block_id=(select max_block_id from max_block_id) "
				+ ") "
				+ "select "
				+ "\"wallet\" ";

		for (SnapshotRequestPolicy snapshotRequestPolicyCurrent : snapshotRequest.getPolicies()) {
			int i = snapshotRequest.getPolicies().indexOf(snapshotRequestPolicyCurrent);
			query += ",string_agg(asset" + i + "_name, ',') asset" + i + "_names ";
			query += ",sum(asset" + i + "_quantity) asset" + i + "_sum ";
		}

		query += "from (";

		for (SnapshotRequestPolicy snapshotRequestPolicyCurrent : snapshotRequest.getPolicies()) {
			int unionQueryIndex = snapshotRequest.getPolicies().indexOf(snapshotRequestPolicyCurrent);

			if (unionQueryIndex > 0) {
				query += "union all ";
			}

			query += "				select "
					+ "				coalesce(sa.\"view\", tx_out.address) \"wallet\" ";
			for (SnapshotRequestPolicy snapshotRequestPolicy : snapshotRequest.getPolicies()) {
				int policyFieldsIndex = snapshotRequest.getPolicies().indexOf(snapshotRequestPolicy);
				if (snapshotRequestPolicy == snapshotRequestPolicyCurrent) {
					query += "				,encode(ma.name, 'escape') asset" + policyFieldsIndex + "_name "
							+ "				,mto.quantity asset" + policyFieldsIndex + "_quantity ";
				} else {
					query += "				,null asset" + policyFieldsIndex + "_name "
							+ "				,0 asset" + policyFieldsIndex + "_quantity ";
				}
			}
			query += "				from multi_asset ma "
					+ "				join ma_tx_out mto on mto.ident=ma.id "
					+ "				join tx_out on tx_out.id=mto.tx_out_id "
					+ "				left join stake_address sa on sa.id=tx_out.stake_address_id "
					+ "				left join tx_in ON tx_out.tx_id=tx_in.tx_out_id AND tx_out.index::smallint=tx_in.tx_out_index::smallint  "
					+ "				where "
					+ "				ma.\"policy\"= decode(?, 'hex') "
					+ "				and tx_out.tx_id<(select max_tx_id from max_tx_id) "
					+ "				and (tx_in.tx_in_id IS null or (tx_in.tx_in_id>=(select max_tx_id from max_tx_id)))";

			unionQueryIndex++;
		}

		query += ") tokens "
				+ "group by \"wallet\" "
				+ "order by \"wallet\"";

		try (Connection connection = hds.getConnection()) {
			connection.createStatement().execute("SET enable_seqscan = OFF; ");

			PreparedStatement ps = connection.prepareStatement(query);
			ps.setTimestamp(1, Timestamp.from(snapshotRequest.getTimestamp().toInstant()));

			int parameterIndex = 2;
			for (SnapshotRequestPolicy snapshotRequestPolicyCurrent : snapshotRequest.getPolicies()) {
				ps.setString(parameterIndex, snapshotRequestPolicyCurrent.getPolicyId());
				parameterIndex++;
			}

			ResultSet result = ps.executeQuery();

			SnapshotResult snapshotResult = new SnapshotResult();
			List<SnapshotResultRow> rows = new ArrayList<>();
			while (result.next()) {
				SnapshotResultRow snapshotResultRow = new SnapshotResultRow();
				snapshotResultRow.setWallet(result.getString("wallet"));

				List<SnapshotResultToken> snapshotResultTokens = new ArrayList<>();
				for (SnapshotRequestPolicy snapshotRequestPolicyCurrent : snapshotRequest.getPolicies()) {
					SnapshotResultToken snapshotResultToken = new SnapshotResultToken();
					int i = snapshotRequest.getPolicies().indexOf(snapshotRequestPolicyCurrent);
					List<String> assetnames = Optional
							.ofNullable(result.getString("asset" + i + "_names"))
							.map(s -> List.of(s.split(",")).stream().distinct().sorted().collect(Collectors.toList()))
							.orElse(List.of());
					snapshotResultToken.setAssetnames(assetnames);
					snapshotResultToken.setAmount(result.getLong("asset" + i + "_sum"));
					snapshotResultTokens.add(snapshotResultToken);
				}
				snapshotResultRow.setSnapshotResultTokens(snapshotResultTokens);
				rows.add(snapshotResultRow);
			}
			snapshotResult.setRows(rows);

			return snapshotResult;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
