package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Entity
@Data
public class MintOnDemandLayerFile {
	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	private String name;

	@NotNull
	private Double weight;
}
