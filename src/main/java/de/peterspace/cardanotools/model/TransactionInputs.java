package de.peterspace.cardanotools.model;

import lombok.Value;

@Value
public class TransactionInputs {
	String txhash;
	int txix;
	long value;
	long stakeAddressId;
	String sourceAddress;
	String policyId;
	byte[] assetNameBytes;
	String metaData;
}
