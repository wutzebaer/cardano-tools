package de.peterspace.cardanotools.cardano;

import java.util.Set;

import org.apache.commons.codec.DecoderException;

/**
 * @author wutze
 *
 *         Credits for this logic go to
 *         https://mantis.functionally.io/how-to/min-ada-value/
 *
 */
public class MinOutputCalculator {

	public static long calculate(Set<String> assetNames, long distinctPolicyIds) throws DecoderException {

		// fixed
		final double minUTxOValue = 1000000;
		final double pidSize = 28; //Hex.decodeHex(mintTransaction.getPolicyId()).length;
		final double coinSize = 0;
		final double utxoEntrySizeWithoutVal = 27;
		final double adaOnlyUTxOSize = utxoEntrySizeWithoutVal + coinSize;
		final double adaPerUTxOWord = 37037;

		// transaction properties
		// the number of distinct PolicyIDs
		double numPIDs = distinctPolicyIds;
		// the number of distinct AssetIDs
		double numAssets = assetNames.size();
		double sumAssetNameLengths = assetNames.stream().mapToDouble(t -> t.length()).sum();

		double size = 6 + Math.floor(((numAssets * 12) + sumAssetNameLengths + (numPIDs * pidSize) + 7) / 8);

		double other = (double) (Math.floor(minUTxOValue / adaOnlyUTxOSize) * (utxoEntrySizeWithoutVal + size));

		// add 100_000 fund tolerance for additional utxos
		return (long) Math.max(minUTxOValue, other) + 100_000;
	}

}
