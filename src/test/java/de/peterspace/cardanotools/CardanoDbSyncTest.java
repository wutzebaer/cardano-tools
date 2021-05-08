package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.dbsync.TokenData;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CardanoDbSyncTest {
//34c5cd85eef8101f6c5235e5bbebbd217d671a89c0cce588315de0e9236eca34

	@Autowired
	CardanoDbSyncClient cardanoDbSyncClient;

	@Test
	void list() throws Exception {
		List<String> inpuAddresses = cardanoDbSyncClient.getInpuAddresses(List.of("34c5cd85eef8101f6c5235e5bbebbd217d671a89c0cce588315de0e9236eca34"));
		assertThat(inpuAddresses).hasSize(1);
		assertEquals("addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", inpuAddresses.get(0));
	}

	@Test
	void single() throws Exception {
		List<String> inpuAddresses = cardanoDbSyncClient.getInpuAddresses("34c5cd85eef8101f6c5235e5bbebbd217d671a89c0cce588315de0e9236eca34");
		assertThat(inpuAddresses).hasSize(1);
		assertEquals("addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x", inpuAddresses.get(0));
	}

	@Test
	void funding() throws Exception {
		List<String> inpuAddresses = cardanoDbSyncClient.getFundingAddresses("addr_test1vpxfv548dwfl5qlq4gd8qhzcv68e33phv72yxgmqqtf9t7g9p0j6x");
		assertThat(inpuAddresses).hasSize(4);
		assertThat(inpuAddresses).contains("addr_test1qqypqe4g9kw9aeuuxp94lcuk0v6k0z79n2f8de8nnm7uwwsxu2hyfhlkwuxupa9d5085eunq2qywy7hvmvej456flknsymw6pl");
		assertThat(inpuAddresses).contains("addr_test1qz98ehykjvsrn34puupda75xm3vyaddh45ygsvaqgmv7fecxu2hyfhlkwuxupa9d5085eunq2qywy7hvmvej456flknsa9zz20");
	}

	@Test
	void getPeterTokenByPolicy() throws DecoderException, JSONException {
		List<TokenData> tokenData = cardanoDbSyncClient.findTokens("b6dec630be794bccc2864538318ccead47bb7fe1a72da5908a9529aa", 0l);
		String policyId = "b6dec630be794bccc2864538318ccead47bb7fe1a72da5908a9529aa";
		assertEquals(policyId, tokenData.get(0).getPolicyId());
		assertEquals("Peter", tokenData.get(0).getName());
		assertEquals(1l, tokenData.get(0).getQuantity());
		assertEquals("5c3c03f830b59d49412ccf5bfba0a9ac6696ed849ff6c7cbc727e6c79b5b879e", tokenData.get(0).getTxId());
		assertEquals("ipfs://QmSvVdL11AXHQzvX9QozV9CZ9j4o8QNwJyJGKkbsiRJHcm", new JSONObject(tokenData.get(0).getJson()).getJSONObject(policyId).getJSONObject("Peter").getString("image"));
		assertEquals(null, tokenData.get(0).getInvalid_before());
		assertEquals(28772044l, tokenData.get(0).getInvalid_hereafter());
		assertEquals(5684470, tokenData.get(0).getBlock_no());
		assertEquals(264, tokenData.get(0).getEpoch_no());
		assertEquals(86698, tokenData.get(0).getEpoch_slot_no());
	}

	@Test
	void getPeterTokenByName() throws DecoderException, JSONException {
		List<TokenData> tokenData = cardanoDbSyncClient.findTokens("peter", 0l);
		String policyId = "b6dec630be794bccc2864538318ccead47bb7fe1a72da5908a9529aa";
		assertEquals(policyId, tokenData.get(0).getPolicyId());
		assertEquals("Peter", tokenData.get(0).getName());
		assertEquals(1l, tokenData.get(0).getQuantity());
		assertEquals("5c3c03f830b59d49412ccf5bfba0a9ac6696ed849ff6c7cbc727e6c79b5b879e", tokenData.get(0).getTxId());
		assertEquals("ipfs://QmSvVdL11AXHQzvX9QozV9CZ9j4o8QNwJyJGKkbsiRJHcm", new JSONObject(tokenData.get(0).getJson()).getJSONObject(policyId).getJSONObject("Peter").getString("image"));
		assertEquals(null, tokenData.get(0).getInvalid_before());
		assertEquals(28772044l, tokenData.get(0).getInvalid_hereafter());
		assertEquals(5684470, tokenData.get(0).getBlock_no());
		assertEquals(264, tokenData.get(0).getEpoch_no());
		assertEquals(86698, tokenData.get(0).getEpoch_slot_no());
		cardanoDbSyncClient.findTokens("peter", 0l);
		cardanoDbSyncClient.findTokens("peter", 0l);
		cardanoDbSyncClient.findTokens("peter", 0l);
	}

	@Test
	void getLatestTokens() throws DecoderException, JSONException {
		List<TokenData> tokenData = cardanoDbSyncClient.latestTokens(null);
		assertEquals(1000, tokenData.size());
		cardanoDbSyncClient.latestTokens(null);
		cardanoDbSyncClient.latestTokens(null);
		cardanoDbSyncClient.latestTokens(null);
		cardanoDbSyncClient.latestTokens(null);
	}

}
