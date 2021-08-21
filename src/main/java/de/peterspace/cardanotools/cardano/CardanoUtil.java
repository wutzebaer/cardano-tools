package de.peterspace.cardanotools.cardano;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Bech32;

import lombok.experimental.UtilityClass;
import ove.crypto.digest.Blake2b;
import ove.crypto.digest.Blake2b.Digest;

@UtilityClass
public class CardanoUtil {

	public static Date calculatePolicyDueDate(Long slot) {
		var lockDate = new Date((1596491091 + (slot - 4924800)) * 1000);
		return lockDate;
	}

	public static String createSubject(String policyId, String assetName) {
		return policyId + encodeBase16(assetName);
	}

	private static String encodeBase16(String content) {
		return new String(new Base16().encode(content.getBytes()));
	}

	public static String createAssetFingerprint(String policyId, String assetName) throws DecoderException {
		byte[] policyIdBytes = Hex.decodeHex(policyId);
		byte[] assetNameBytes = assetName.getBytes();
		Digest blakeDigest = Blake2b.Digest.newInstance(20);
		byte[] blakeHash = blakeDigest.digest(ArrayUtils.addAll(policyIdBytes, assetNameBytes));

		List<Integer> words = convertBits(blakeHash, 8, 5, false);
		byte[] bytes = new byte[words.size()];
		for (int i = 0; i < words.size(); i++) {
			bytes[i] = words.get(i).byteValue();
		}

		String bechString = Bech32.encode("asset", bytes);
		return bechString;
	}

	private static List<Integer> convertBits(byte[] data, int fromWidth, int toWidth, boolean pad) {
		int acc = 0;
		int bits = 0;
		int maxv = (1 << toWidth) - 1;
		List<Integer> ret = new ArrayList<>();

		for (int i = 0; i < data.length; i++) {
			int value = data[i] & 0xff;
			if (value < 0 || value >> fromWidth != 0) {
				return null;
			}
			acc = (acc << fromWidth) | value;
			bits += fromWidth;
			while (bits >= toWidth) {
				bits -= toWidth;
				ret.add((acc >> bits) & maxv);
			}
		}

		if (pad) {
			if (bits > 0) {
				ret.add((acc << (toWidth - bits)) & maxv);
			} else if (bits >= fromWidth || ((acc << (toWidth - bits)) & maxv) != 0) {
				return null;
			}
		}
		return ret;
	}
}
