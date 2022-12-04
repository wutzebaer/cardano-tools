package de.peterspace.cardanotools.rest;

import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.repository.AccountRepository;
import de.peterspace.cardanotools.rest.dto.AccountStatementRow;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statement")
public class WalletStatementRestInterface {

	private final AccountRepository accountRepository;
	private final CardanoDbSyncClient cardanoDbSyncClient;

	@GetMapping("{stakeAddress}")
	@Cacheable("accountStatement")
	public List<AccountStatementRow> accountStatement(@PathVariable("stakeAddress") String stakeAddress) throws DecoderException {
		return cardanoDbSyncClient.accountStatement(stakeAddress);
	}

}
