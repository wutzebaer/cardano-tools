package de.peterspace.cardanotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

import de.peterspace.cardanotools.cardano.CardanoUtil;

public class CardanoUtilTest {
	@Test
	void testAssetFingerprint() throws DecoderException {
		final String policyId = "2697bec9e609932eaaaafb34c8e1ea706549d268166ef60285026b85";
		final String assetName = "redomgrun";
		final String expectedFingerprint = "asset107k0vzcplp8rjc3d2y8cew6z2gdhvnqru2uluu";
		String fingerprint = CardanoUtil.createAssetFingerprint(policyId, assetName);
		assertEquals(expectedFingerprint, fingerprint);
	}
}
