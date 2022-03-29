package de.peterspace.cardanotools.ipfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.api.IPFS.PinType;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IpfsClient {

	@Value("${ipfs.api.url}")
	private String apiUrl;

	public String addFile(InputStream is) throws Exception {
		IPFS ipfs = new IPFS(apiUrl);
		NamedStreamable.InputStreamWrapper data = new NamedStreamable.InputStreamWrapper(is);
		MerkleNode result = ipfs.add(data).get(0);
		// ipfs.pin.add(result.hash);
		return result.hash.toBase58();
	}

	public void pinFile(String ipfsUrl) throws Exception {
		IPFS ipfs = new IPFS(apiUrl);
		// Map<Multihash, Object> ls = ipfs.pin.ls(PinType.recursive);
		ipfs.pin.add(Multihash.fromBase58(StringUtils.right(ipfsUrl, 46)));
	}

	public Integer getSize(String ipfsUrl) throws IOException {
		IPFS ipfs = new IPFS(apiUrl);
		return (Integer) ipfs.object.stat(Multihash.fromBase58(StringUtils.right(ipfsUrl, 46))).get("CumulativeSize");
	}

}
