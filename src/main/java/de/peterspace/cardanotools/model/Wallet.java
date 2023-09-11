package de.peterspace.cardanotools.model;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.rest.dto.Views.Persisted;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(name = "wallet_drop_stake_unique", columnNames = { "drop_id", "stakeaddress" }))
public class Wallet {

	@Id
	@GeneratedValue
	@JsonView(Persisted.class)
	@NotNull
	private Long id;

	@NotNull
	@ManyToOne
	private Drop drop;

	@NotNull
	private String stakeAddress;

	@NotNull
	@Min(0)
	private Integer tokensMinted = 0;

}
