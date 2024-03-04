package de.peterspace.cardanotools.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintingStatus {
	@Id
	@NotBlank
	String paymentTxId;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	String status;

	@NotNull
	Long validUntilSlot;

	String txId;

	@NotNull
	Boolean finished;

	@NotNull
	Boolean finalStep;
}