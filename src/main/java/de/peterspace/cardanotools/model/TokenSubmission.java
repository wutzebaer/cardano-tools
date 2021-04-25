package de.peterspace.cardanotools.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONObject;

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

	@NotBlank
	private String assetName;

	@NotNull
	private Long amount;

	@NotNull
	@OneToMany(cascade = CascadeType.ALL)
	private Map<String, MetaValue> metaData;

	@JsonIgnore
	public JSONObject getCleanedMetadata() {
		if (metaData.size() > 0) {
			JSONObject targetObject = new JSONObject();
			for (Entry<String, MetaValue> metaEntry : metaData.entrySet()) {
				MetaValue valueObject = metaEntry.getValue();
				if (!valueObject.getListValue().isEmpty()) {
					targetObject.put(metaEntry.getKey().toLowerCase(), new JSONArray(valueObject.getListValue()));
				} else {
					targetObject.put(metaEntry.getKey().toLowerCase(), valueObject.getValue());
				}
			}
			return targetObject;
		} else {
			return null;
		}
	}

}
