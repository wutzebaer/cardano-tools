package de.peterspace.cardanotools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.repository.MintTransactionRepository;
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
}
