package de.peterspace.cardanotools.model;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
