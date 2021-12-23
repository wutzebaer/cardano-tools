package de.peterspace.cardanotools.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "mint_Order_Submission_id", "assetname" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenSubmission {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotNull
	private String assetName;

	@NotNull
	private Long amount;

}
