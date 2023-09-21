package de.peterspace.cardanotools.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class PriceDto {
	@NotNull
	private LocalDate date;

	@NotNull
	private BigDecimal value;
}
