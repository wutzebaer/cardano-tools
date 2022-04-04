package de.peterspace.cardanotools.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StakeRewardSubmission {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;

	@NotNull
	private Boolean tip;

	@NotNull
	private Map<String, Map<String, Long>> outputs = new HashMap<>();

}