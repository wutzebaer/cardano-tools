package de.peterspace.cardanotools.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import de.peterspace.cardanotools.model.Account;
import de.peterspace.cardanotools.model.MintOrder;
import de.peterspace.cardanotools.model.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintOrderSubmission {

	@NotEmpty
	private List<TokenSubmission> tokens;

	@NotBlank
	private String targetAddress;

	@NotNull
	private ChangeAction changeAction;

	@NotNull
	private Long fee;

	public MintOrder toMintOrder(Account account) {
		MintOrder mintOrder = new MintOrder();
		mintOrder.setAccount(account);
		mintOrder.setCreatedAt(new Date());

		List<Token> tokens = new ArrayList<>();
		for (TokenSubmission tokenSubmission : getTokens()) {
			tokens.add(tokenSubmission.toToken(mintOrder));
		}
		mintOrder.setTokens(tokens);
		mintOrder.setTargetAddress(getTargetAddress());
		mintOrder.setChangeAction(getChangeAction());
		return mintOrder;
	}

}