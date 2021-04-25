package de.peterspace.cardanotools.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "mint_Order_id", "assetname" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@ManyToOne
	private MintOrder mintOrder;

	@NotBlank
	private String assetName;

	@NotNull
	private Long amount;

	@Column(columnDefinition = "TEXT")
	private String metaDataJson;

}
