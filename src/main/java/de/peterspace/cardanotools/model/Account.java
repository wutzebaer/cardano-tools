package de.peterspace.cardanotools.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotNull;
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
	private List<StakePosition> stakePositions;

	@NotNull
	private Long stake;

	@NotNull
	private Date lastUpdate;

	@NotNull
	@JsonIgnore
	@Column(columnDefinition = "bool default false")
	private Boolean freePin;

	public Policy getPolicy(String policyId) {
		return policies.stream().filter(p -> p.getPolicyId().equals(policyId)).findAny().get();
	}

}