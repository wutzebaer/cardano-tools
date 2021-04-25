package de.peterspace.cardanotools.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MetaValue {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	private String value;

	@NotNull
	@ElementCollection
	private List<String> listValue;
}
