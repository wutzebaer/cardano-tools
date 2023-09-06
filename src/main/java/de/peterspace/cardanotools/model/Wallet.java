package de.peterspace.cardanotools.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

	@Id
	@NotNull
	private Long stakeAddressId;

	@NotNull
	@Min(0)
	Integer tokensMinted = 0;

}
