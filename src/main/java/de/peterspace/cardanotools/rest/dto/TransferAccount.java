package de.peterspace.cardanotools.rest.dto;

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

	public static TransferAccount from(Account account) {
		return new TransferAccount(account.getKey(), account.getAddress(), account.getBalance());
	}

}
