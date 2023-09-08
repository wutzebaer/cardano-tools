package de.peterspace.cardanotools.cardano;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class MissingLovelaceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long amount;

	public MissingLovelaceException(long amount, String message, Throwable cause) {
		super(message, cause);
		this.amount = amount;
	}

}
