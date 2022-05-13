package de.peterspace.cardanotools.cardano;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TransactionOutputs {

	private Map<String, Map<String, Long>> outputs = new HashMap<>();

	public void add(String address, String currency, long amount) {
		Map<String, Long> addressMap = outputs.computeIfAbsent(address, k -> new HashMap<>());
		Long currentAmount = addressMap.computeIfAbsent(currency, k -> 0l);
		if (currentAmount + amount != 0) {
			addressMap.put(currency, currentAmount + amount);
		} else {
			addressMap.remove(currency);
		}
		if (addressMap.isEmpty()) {
			outputs.remove(address);
		}
	}

	public List<String> toCliFormat() {
		return outputs
				.entrySet().stream()
				.map(addressEntry -> addressEntry.getKey() + "+" +
						addressEntry.getValue()
								.entrySet().stream()
								.map(currencyEntry -> (currencyEntry.getValue() + " " + currencyEntry.getKey()).trim())
								.collect(Collectors.joining("+"))

				)
				.collect(Collectors.toList());
	}

	public String toCliFormat(String address) {
		return outputs
				.entrySet().stream()
				.filter(addressEntry -> addressEntry.getKey().equals(address))
				.map(addressEntry -> addressEntry.getKey().split("#")[0] + "+" +
						addressEntry.getValue()
								.entrySet().stream()
								.map(currencyEntry -> (currencyEntry.getValue() + " " + currencyEntry.getKey()).trim())
								.collect(Collectors.joining("+"))

				)
				.findFirst().orElse("");
	}

}
