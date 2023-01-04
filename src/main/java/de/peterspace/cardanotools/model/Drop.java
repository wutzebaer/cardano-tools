package de.peterspace.cardanotools.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.model.Views.Persisted;
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
	private Set<String> whitelist;

	@NotNull
	@ElementCollection
	@OrderColumn(name = "sequence")
	private List<DropNft> dropNfts;

	@NotNull
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> dropNftsSoldAssetNames;

	@NotNull
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> dropNftsAvailableAssetNames;

	@NotNull
	@NotEmpty
	private String prettyUrl;
	
	@NotNull
	@JsonView(Persisted.class)
	private long fee = 1000000;

}