package de.peterspace.cardanotools.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.peterspace.cardanotools.model.Price;
import de.peterspace.cardanotools.repository.PriceRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceService {

	private static final LocalDate INITIAL_DATE = LocalDate.parse("2017-10-01");

	private final PriceRepository priceRepository;

	@Getter
	private final Map<LocalDate, Price> prices = new HashMap<>();

	@Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
	@Scheduled(cron = "0 0 * * * *")
	public void createSnapshot() throws MalformedURLException, IOException, InterruptedException {
		log.info("Creating Snapshot");
		LocalDate currentDate = priceRepository
				.getLastDate()
				.map(c -> c.plus(1, ChronoUnit.DAYS))
				.orElse(INITIAL_DATE);

		for (Price price : priceRepository.findAll()) {
			prices.put(price.getDate(), price);
		}

		while (!currentDate.isAfter(LocalDate.now())) {
			try {
				log.info("Creating Snapshot for {}", currentDate);

				String url = "https://api.coingecko.com/api/v3/coins/cardano/history?date=" + currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

				log.info("Fetching {}", url);
				String data = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);

				Price price = new Price();
				price.setCoin("cardano");
				price.setData(data);
				price.setDate(currentDate);
				priceRepository.save(price);
				prices.put(price.getDate(), price);

				currentDate = currentDate.plus(1, ChronoUnit.DAYS);
			} catch (IOException e) {
				log.error("Error while fetching price", e);
				Thread.sleep(60_000);
			}
		}

	}

}
