package de.peterspace.cardanotools.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintTransaction {

	@Id
	private String key;

	@Column(columnDefinition = "TEXT")
	private String rawData;

	@Column(columnDefinition = "TEXT")
	private String metaDataJson;

}
