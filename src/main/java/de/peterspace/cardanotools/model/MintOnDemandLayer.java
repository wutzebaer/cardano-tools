package de.peterspace.cardanotools.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
public class MintOnDemandLayer {
	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	private String name;

	@NotNull
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn
	private List<MintOnDemandLayerFile> files;
}
