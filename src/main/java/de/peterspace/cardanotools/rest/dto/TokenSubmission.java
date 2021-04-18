package de.peterspace.cardanotools.rest.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import de.peterspace.cardanotools.model.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenSubmission {

	@NotBlank
	private String assetName;

	@NotNull
	private Long amount;

	private String metaDataJson;

	public Token toToken() {
		Token token = new Token();
		token.setAssetName(getAssetName());
		token.setAmount(getAmount());
		token.setMetaDataJson(metaDataJson);
		return token;
	}

}
