package de.peterspace.cardanotools.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.model.Views.Private;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Address {

	public Address(String address, String skey, String vkey, Long balance, String tokensData) {
		super();
		this.address = address;
		this.skey = skey;
		this.vkey = vkey;
		this.balance = balance;
		this.tokensData = tokensData;
	}

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotBlank
	private String address;

	@NotBlank
	@JsonView(Private.class)
	private String skey;

	@NotBlank
	@JsonView(Private.class)
	private String vkey;

	@NotNull
	private Long balance;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	String tokensData;

}