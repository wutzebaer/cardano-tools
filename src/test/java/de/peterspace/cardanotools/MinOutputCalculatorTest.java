package de.peterspace.cardanotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import de.peterspace.cardanotools.cardano.MinOutputCalculator;
import de.peterspace.cardanotools.model.MintOrderSubmission;
import de.peterspace.cardanotools.model.MintTransaction;
import de.peterspace.cardanotools.model.TokenSubmission;

public class MinOutputCalculatorTest {

	@Test
	void calculateMinOutputSize1() throws Exception {

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAssetName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		tokens.add(token1);

		long calculateMinOutputSize = MinOutputCalculator.calculate(tokens);

		assertEquals(1555554, calculateMinOutputSize);
	}

	@Test
	void calculateMinOutputSize2() throws Exception {

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAssetName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAssetName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1");
		tokens.add(token2);

		long calculateMinOutputSize = MinOutputCalculator.calculate(tokens);

		assertEquals(1777776, calculateMinOutputSize);
	}

	@Test
	void calculateMinOutputSize3() throws Exception {

		ArrayList<TokenSubmission> tokens = new ArrayList<TokenSubmission>();

		TokenSubmission token1 = new TokenSubmission();
		token1.setAssetName("A");
		tokens.add(token1);

		TokenSubmission token2 = new TokenSubmission();
		token2.setAssetName("A");
		tokens.add(token2);

		long calculateMinOutputSize = MinOutputCalculator.calculate(tokens);

		assertEquals(1481480, calculateMinOutputSize);
	}

}
