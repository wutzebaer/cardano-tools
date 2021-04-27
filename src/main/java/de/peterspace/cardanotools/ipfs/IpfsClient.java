package de.peterspace.cardanotools.ipfs;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IpfsClient {

	@Value("${ipfs.api.url}")
	private String apiUrl;

	private IPFS ipfs;

	@PostConstruct
	public void init() {
		ipfs = new IPFS(apiUrl);
	}

	public String addFile(InputStream is) throws Exception {
		NamedStreamable.InputStreamWrapper data = new NamedStreamable.InputStreamWrapper(is);
		MerkleNode result = ipfs.add(data).get(0);
		ipfs.pin.add(result.hash);
		return result.hash.toBase58();
	}

}
