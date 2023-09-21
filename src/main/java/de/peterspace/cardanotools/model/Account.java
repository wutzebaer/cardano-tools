package de.peterspace.cardanotools.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

	@NotNull
	@JsonIgnore
	@Column(columnDefinition = "bool default false")
	private Boolean freePin;

}