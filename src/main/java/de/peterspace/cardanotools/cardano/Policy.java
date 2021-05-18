package de.peterspace.cardanotools.cardano;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Policy {
	private String policy;
	private String policyId;
	private Date policyDueDate;
}
