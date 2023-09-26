package de.peterspace.cardanotools.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import de.peterspace.cardanotools.rest.dto.Views.Private;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Address {

	public Address(String address, String skey, String vkey) {
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
	@JsonView(Private.class)
	private String skey;

	@NotBlank
	@JsonView(Private.class)
	private String vkey;

}