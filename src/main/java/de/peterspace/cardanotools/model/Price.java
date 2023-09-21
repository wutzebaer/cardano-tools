package de.peterspace.cardanotools.model;

import java.time.LocalDate;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "date", "coin" }) })
@Data
public class Price {

	@Id
	@GeneratedValue
	@NotNull
	private Long id;

	@NotNull
	private LocalDate date;

	@NotNull
	private String coin;

	@NotNull
	@Column(columnDefinition = "text")
	private String data;

	@Transient
	private JSONObject parsedData;

	@JsonIgnore
	public JSONObject getParsedData() {
		if (parsedData == null) {
			parsedData = new JSONObject(data);
		}
		return parsedData;
	}

}
