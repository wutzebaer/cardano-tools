package de.peterspace.cardanotools.cardano;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class MissingLovelaceException extends Exception {

	long amount;

	public MissingLovelaceException(long amount, String message, Throwable cause) {
		super(message, cause);
		this.amount = amount;
	}

}
