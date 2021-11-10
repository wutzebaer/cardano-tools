package de.peterspace.cardanotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.peterspace.cardanotools.cardano.MinOutputCalculator;

public class MinOutputCalculatorTest {

	@Test
	void calculateMinOutputSize1() throws Exception {
		long calculateMinOutputSize = MinOutputCalculator.calculate(Set.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), 1);
		assertEquals(1555554, calculateMinOutputSize);
	}

	@Test
	void calculateMinOutputSize2() throws Exception {
		long calculateMinOutputSize = MinOutputCalculator.calculate(Set.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1"), 1);
		assertEquals(1777776, calculateMinOutputSize);
	}

	@Test
	void calculateMinOutputSize3() throws Exception {
		long calculateMinOutputSize = MinOutputCalculator.calculate(Set.of("A"), 1);
		assertEquals(1444443, calculateMinOutputSize);
	}

}
