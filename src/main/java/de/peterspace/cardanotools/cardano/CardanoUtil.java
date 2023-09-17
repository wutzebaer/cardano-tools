
package de.peterspace.cardanotools.cardano;

import java.util.Date;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CardanoUtil {

	public static Date calculatePolicyDueDate(Long slot) {
		var lockDate = new Date((1596491091 + (slot - 4924800)) * 1000);
		return lockDate;
	}

	public static long currentSlot() {
		return System.currentTimeMillis() / 1000 - 1596491091 + 4924800;
	}

	public static String createSubject(String policyId, String assetName) {
		return policyId + assetName;
	}

}
