package de.peterspace.cardanotools.cardano;

import java.util.List;

import org.apache.commons.codec.DecoderException;

import de.peterspace.cardanotools.model.TokenSubmission;

/**
 * @author wutze
 *
 *         Credits for this logic go to
 *         https://mantis.functionally.io/how-to/min-ada-value/
 *
 */
public class MinOutputCalculator {

	public static long calculate(List<TokenSubmission> tokens) throws DecoderException {

		// fixed
		final double minUTxOValue = 1000000;
		final double pidSize = 28; //Hex.decodeHex(mintTransaction.getPolicyId()).length;
		final double coinSize = 0;
		final double utxoEntrySizeWithoutVal = 27;
		final double adaOnlyUTxOSize = utxoEntrySizeWithoutVal + coinSize;
		final double adaPerUTxOWord = 37037;

		// transaction properties
		double numPIDs = 1;
		double numAssets = tokens.size();
		double sumAssetNameLengths = tokens.stream().mapToDouble(t -> t.getAssetName().length()).sum();

		double size = 6 + Math.floor(((numAssets * 12) + sumAssetNameLengths + (numPIDs * pidSize) + 7) / 8);

		double other = (double) (Math.floor(minUTxOValue / adaOnlyUTxOSize) * (utxoEntrySizeWithoutVal + size));

		return (long) Math.max(minUTxOValue, other);
	}

}
