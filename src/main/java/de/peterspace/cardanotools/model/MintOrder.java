package de.peterspace.cardanotools.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintOrder {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	private Date createdAt;

	@ManyToOne
	@NotNull
	private Account account;

	@OneToMany(mappedBy = "mintOrder", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<Token> tokens;

	@JsonIgnore
	private String txid;

	@JsonIgnore
	private String policyScript;

	public String createFilePrefix() {
		return account.getKey() + "_mintorder_" + id;
	}

}