package de.peterspace.cardanotools.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.rest.dto.Views.Persisted;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(indexes = @Index(columnList = "prettyUrl", unique = true))
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
	@ElementCollection
	private List<String> whitelist;

	@NotNull
	@ElementCollection
	@OrderColumn(name = "sequence")
	private List<DropNft> dropNfts;

	@NotNull
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> dropNftsSoldAssetNames;

	@NotNull
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> dropNftsAvailableAssetNames;

	@NotNull
	@NotEmpty
	private String prettyUrl;

	@NotNull
	@JsonView(Persisted.class)
	private long fee = 1000000;

}