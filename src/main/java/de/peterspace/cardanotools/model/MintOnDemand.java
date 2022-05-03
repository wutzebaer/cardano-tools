package de.peterspace.cardanotools.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "policy_id" }) })
public class MintOnDemand {
	@Id
	@GeneratedValue
	private Long id;

	@OneToOne
	private Policy policy;

	@NotNull
	private Integer width;

	@NotNull
	private Integer height;

	@NotNull
	private Long amount;

	@Column(columnDefinition = "TEXT", nullable = false)
	@NotBlank
	private String metadataString;

	@NotNull
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn
	private List<MintOnDemandLayer> layers;
}
