package de.peterspace.cardanotools.cardano;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class TransactionOutputs {

	private Map<String, Map<String, Long>> outputs = new HashMap<>();

	public void add(String address, String currency, long amount) {
		Map<String, Long> addressMap = outputs.computeIfAbsent(address, k -> new HashMap<>());
		Long currentAmount = addressMap.computeIfAbsent(currency, k -> 0l);
		addressMap.put(currency, currentAmount + amount);
	}

	public List<String> toCliFormat() {
		return outputs
				.entrySet().stream()
				.map(addressEntry -> addressEntry.getKey() + "+" +
						addressEntry.getValue()
								.entrySet().stream()
								.filter(currencyEntry -> currencyEntry.getValue() > 0)
								.map(currencyEntry -> (currencyEntry.getValue() + " " + currencyEntry.getKey()).trim())
								.collect(Collectors.joining("+"))

				)
				.collect(Collectors.toList());
	}

}
