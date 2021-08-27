package de.peterspace.cardanotools.dbsync;

import lombok.Value;

@Value
public class OfferTokenFunding {
	String address;
	String policy;
	String name;
	Long quantity;
}
