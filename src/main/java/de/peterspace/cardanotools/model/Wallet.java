package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
