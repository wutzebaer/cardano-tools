package de.peterspace.cardanotools.rest.dto;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import de.peterspace.cardanotools.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferAccount {

	@NotBlank
	private String key;

	@NotBlank
	private String address;

	@NotNull
	private Long balance;

	@NotNull
	private List<String> fundingAddresses;

	public static TransferAccount from(Account account) {
		return new TransferAccount(account.getKey(), account.getAddress(), account.getBalance(), account.getFundingAddresses());
	}

}
