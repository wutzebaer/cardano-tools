package de.peterspace.cardanotools.cardano;

import java.util.List;

import lombok.Data;

@Data
public class CardanoTransaction {
	private List<String> in;
	private List<String> out;
	private List<String> mint;
}
