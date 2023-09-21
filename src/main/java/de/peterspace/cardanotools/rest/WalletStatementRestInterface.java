package de.peterspace.cardanotools.rest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.peterspace.cardanodbsyncapi.client.model.AccountStatementRow;
import de.peterspace.cardanotools.dbsync.CardanoDbSyncClient;
import de.peterspace.cardanotools.model.Price;
import de.peterspace.cardanotools.rest.dto.PriceDto;
import de.peterspace.cardanotools.rest.mapper.PriceMapper;
import de.peterspace.cardanotools.service.PriceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statement")
public class WalletStatementRestInterface {

	private final CardanoDbSyncClient cardanoDbSyncClient;
	private final PriceService priceService;
	private final PriceMapper priceMapper;

	@GetMapping("{stakeAddress}")
	public List<AccountStatementRow> accountStatement(@PathVariable("stakeAddress") String stakeAddresses, @RequestParam String currency) throws DecoderException {

		List<String> addressList = Arrays.asList(stakeAddresses.split("\\s+"));
		return addressList
				.parallelStream()
				.flatMap(s -> cardanoDbSyncClient.getStatement(s).stream())
				.sorted(Comparator.comparing(AccountStatementRow::getTimestamp))
				.collect(Collectors.toList());

	}

	@GetMapping("prices")
	@Cacheable("prices")
	public Map<LocalDate, PriceDto> prices(String currency) throws DecoderException {
		Map<LocalDate, Price> prices = priceService.getPrices();
		return prices
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						e -> e.getKey(),
						e -> priceMapper.toDto(e.getValue(), currency)));
	}

}
