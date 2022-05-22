package de.peterspace.cardanotools.ipfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.ipfs.api.IPFS;
import io.ipfs.api.IPFS.PinType;
import io.ipfs.multiaddr.MultiAddress;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpfsClient {

	@Value("${ipfs.api.url}")
	private String apiUrl;

	private Map<Multihash, Object> pins = new HashMap<>();

	private IPFS buildIpfsClient() {
		MultiAddress multiAddress = new MultiAddress(apiUrl);
		IPFS ipfs = new IPFS(multiAddress.getHost(), multiAddress.getTCPPort(), "/api/v0/", 10_000, 60_000, multiAddress.toString().contains("/https"));
		return ipfs;
	}

	@Async
	@Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
	public void updatePins() throws IOException {
		log.info("Updating pins");
		IPFS ipfs = buildIpfsClient();
		Map<Multihash, Object> ls = ipfs.pin.ls(PinType.recursive);
		this.pins = ls;
		log.info("Updating pins: {} pins found", pins.size());
	}

	public String addFile(InputStream is) throws Exception {
		IPFS ipfs = buildIpfsClient();
		NamedStreamable.InputStreamWrapper data = new NamedStreamable.InputStreamWrapper(is);
		MerkleNode result = ipfs.add(data).get(0);
		if (!pins.containsKey(result.hash)) {
			ipfs.pin.rm(result.hash, true);
		}
		return result.hash.toBase58();
	}

	public void unpinFile(String ipfsUrl) throws Exception {
		IPFS ipfs = buildIpfsClient();
		ipfs.pin.rm(Multihash.fromBase58(StringUtils.right(ipfsUrl, 46)), true);
	}

	public void pinFile(String ipfsUrl) throws Exception {
		IPFS ipfs = buildIpfsClient();
		ipfs.pin.add(Multihash.fromBase58(StringUtils.right(ipfsUrl, 46)));
	}

	public Integer getSize(String ipfsUrl) throws IOException {
		IPFS ipfs = buildIpfsClient();
		return (Integer) ipfs.object.stat(Multihash.fromBase58(StringUtils.right(ipfsUrl, 46))).get("CumulativeSize");
	}

}
