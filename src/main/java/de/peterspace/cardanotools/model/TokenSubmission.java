package de.peterspace.cardanotools.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
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
