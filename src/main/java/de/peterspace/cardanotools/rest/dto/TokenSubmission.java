package de.peterspace.cardanotools.rest.dto;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import de.peterspace.cardanotools.model.MintOrder;
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

	@NotNull
	private Map<String, MetaValue> metaData;

	public Token toToken(MintOrder mintOrder) {
		Token token = new Token();
		token.setAssetName(getAssetName());
		token.setAmount(getAmount());
		token.setMetaDataJson(new JSONObject(metaData).toString(3));
		token.setMintOrder(mintOrder);
		return token;
	}

}
