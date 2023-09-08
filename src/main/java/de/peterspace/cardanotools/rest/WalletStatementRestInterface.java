package de.peterspace.cardanotools.rest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	public List<AccountStatementRow> accountStatement(@PathVariable("stakeAddress") String stakeAddresses, @RequestParam String currency) throws DecoderException {

		List<String> addressList = Arrays.asList(stakeAddresses.split("\\s+"));
		return addressList
				.parallelStream()
				.flatMap(s -> cardanoDbSyncClient.accountStatement(s, currency).stream())
				.sorted(Comparator.comparing(AccountStatementRow::getTimestamp))
				.collect(Collectors.toList());

	}

}
