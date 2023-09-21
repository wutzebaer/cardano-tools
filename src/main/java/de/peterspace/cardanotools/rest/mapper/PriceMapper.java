package de.peterspace.cardanotools.rest.mapper;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.peterspace.cardanotools.model.Price;
import de.peterspace.cardanotools.rest.dto.PriceDto;

@Mapper
public interface PriceMapper {

	@Mapping(target = "value", expression = "java(getValueSafely(price, currency))")
	PriceDto toDto(Price price, String currency);

	default BigDecimal getValueSafely(Price price, String currency) {
		JSONObject parsedData = price.getParsedData();
		if (parsedData == null)
			return null; // Or return some default value

		JSONObject marketData = parsedData.optJSONObject("market_data");
		if (marketData == null)
			return null; // Or return some default value

		JSONObject currentPrice = marketData.optJSONObject("current_price");
		if (currentPrice == null)
			return null; // Or return some default value

		return currentPrice.optBigDecimal(currency, null); // You can replace null with a default value if needed
	}

}
