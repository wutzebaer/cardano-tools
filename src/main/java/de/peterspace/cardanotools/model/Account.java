package de.peterspace.cardanotools.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

	@Id
	@NotNull
	private String key;

	@NotNull
	private Date createdAt;

	@NotBlank
	private String address;

	@NotBlank
	private String skey;

	@NotBlank
	private String vkey;

	@NotNull
	@ElementCollection
	private List<String> fundingAddresses;

	@NotNull
	@Min(0)
	@Column(columnDefinition = "bigint DEFAULT 0")
	private Long balance;

	@NotNull
	@Column(columnDefinition = "bigint DEFAULT 0")
	private Long lastUpdate;

	@Column(columnDefinition = "TEXT")
	@NotBlank
	private String policy;

	@NotBlank
	private String policyId;

	@NotNull
	@Column(columnDefinition = "timestamp default '1970-01-01'")
	private Date policyDueDate;
}