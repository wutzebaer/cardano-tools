package de.peterspace.cardanotools.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

	@Id
	@NotNull
	private String key;

	@NotNull
	private Date createdAt;

	@NotNull
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "account", fetch = FetchType.EAGER)
	@OrderBy("id DESC")
	private List<Policy> policies;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

	@NotNull
	@ElementCollection
	private List<String> fundingAddresses;

	@NotNull
	@ElementCollection
	private List<String> fundingAddressesHistory;

	@NotNull
	private Long stake;

	@NotNull
	private Date lastUpdate;

	public Policy getPolicy(String policyId) {
		return policies.stream().filter(p -> p.getPolicyId().equals(policyId)).findAny().get();
	}

}