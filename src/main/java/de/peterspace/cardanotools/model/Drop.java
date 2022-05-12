package de.peterspace.cardanotools.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.model.Views.Persisted;
import lombok.Data;

@Entity
@Data
public class Drop {

	@Id
	@GeneratedValue
	@JsonView(Persisted.class)
	@NotNull
	private Long id;

	@NotNull
	@ManyToOne
	@JsonView(Persisted.class)
	private Policy policy;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	@JsonView(Persisted.class)
	private Address address;

	@NotNull
	@NotEmpty
	private String name;

	@NotNull
	private long price;

	@NotNull
	private int maxPerTransaction;

	@NotNull
	private boolean running;

	@NotNull
	@NotEmpty
	private String profitAddress;

	@NotNull
	@Column(columnDefinition = "TEXT")
	private String whitelist;

	@NotNull
	@ElementCollection
	@OrderColumn(name="sequence")
	private List<DropNft> dropNfts;

	@NotNull
	@ElementCollection
	private Set<String> dropNftsSoldAssetNames;

	@NotNull
	@ElementCollection
	private Set<String> dropNftsAvailableAssetNames;

}