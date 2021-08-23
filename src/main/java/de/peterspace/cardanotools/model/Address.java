package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Address {

	public Address(@NotBlank String address, @NotBlank String skey, @NotBlank String vkey) {
		super();
		this.address = address;
		this.skey = skey;
		this.vkey = vkey;
	}

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotBlank
	private String address;

	@NotBlank
	private String skey;

	@NotBlank
	private String vkey;

}