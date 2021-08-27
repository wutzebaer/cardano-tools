package de.peterspace.cardanotools.cardano;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public void substractFees(long fees) throws Exception {
		while (fees > 0) {
			Optional<Map<String, Long>> highestOutputOptional = outputs.values()
					.stream()
					.filter(m -> m.keySet().equals(Set.of("")))
					.filter(m -> m.getOrDefault("", 0l) > 1000000)
					.sorted(Comparator.comparingLong(m -> ((Map<String, Long>) m).getOrDefault("", 0l)).reversed())
					.findFirst();
			if (highestOutputOptional.isEmpty()) {
				throw new Exception("Not enough ada to substract fees: " + outputs);
			} else {
				Map<String, Long> highestOutput = highestOutputOptional.get();
				highestOutput.put("", highestOutputOptional.get().get("") - 1);
			}
			fees--;
		}
	}

}
