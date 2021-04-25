package de.peterspace.cardanotools.rest.dto;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

		JSONObject sourceObject = new JSONObject(metaData);
		JSONObject targetObject = new JSONObject();
		if (sourceObject.length() > 0) {
			Iterator<String> keyIterator = sourceObject.keys();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				JSONObject valueObject = sourceObject.getJSONObject(key);
				if (!valueObject.getJSONArray("listValue").isEmpty()) {
					targetObject.put(key.toLowerCase(), valueObject.getJSONArray("listValue"));
				} else {
					targetObject.put(key.toLowerCase(), valueObject.get("value"));
				}
			}
			token.setMetaDataJson(targetObject.toString(3));
		}

		token.setMintOrder(mintOrder);
		return token;
	}

}
