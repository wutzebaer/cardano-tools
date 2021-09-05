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

	@Test
	void testAssetSubject() throws DecoderException {
		final String policyId = "33aeeac38bbc7776c6e2c4787b24e99ffe8a17fb6829456492040d28";
		final String assetName = "VivaFidel";
		final String expectedSubject = "33aeeac38bbc7776c6e2c4787b24e99ffe8a17fb6829456492040d2856697661466964656c";
		String subject = CardanoUtil.createSubject(policyId, assetName);
		assertEquals(expectedSubject, subject);
	}
	@Test
	void testAssetSubject2() throws DecoderException {
		final String policyId = "b5b93f0b9e81fac0e74efd4dfffd713a2f24c70bac0c30f7a1a0a0e3";
		final String assetName = "EUR";
		final String expectedSubject = "b5b93f0b9e81fac0e74efd4dfffd713a2f24c70bac0c30f7a1a0a0e3455552";
		String subject = CardanoUtil.createSubject(policyId, assetName);
		assertEquals(expectedSubject, subject);
	}
}
