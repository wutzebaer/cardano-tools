package de.peterspace.cardanotools.model;

import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@NotBlank
	private String assetName;

	@NotNull
	private Long amount;

	@Column(columnDefinition = "TEXT DEFAULT '{}'")
	@NotBlank
	private String metaData;

}
