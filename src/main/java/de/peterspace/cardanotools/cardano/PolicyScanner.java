package de.peterspace.cardanotools.cardano;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanotools.process.ProcessUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class PolicyScanner {

	@Value("${network}")
	private String network;

	@Value("${working.dir}")
	private String workingDir;

	private final FileUtil fileUtil;
	private final CardanoNode cardanoNode;
	private File outputPath;

	@Getter
	private final Map<String, String> policies = new HashMap<>();

	@PostConstruct
	public void init() throws Exception {
		outputPath = new File(workingDir, "policy-scripts");
		outputPath.mkdirs();
		if (outputPath.listFiles().length == 0) {
			extractPolicies();
		}
		readPolicies();
	}

	@Scheduled(cron = "0 0 * * * *")
	public void updatePolicies() throws Exception {
		extractPolicies();
		readPolicies();
	}

	private void extractPolicies() throws Exception {
		log.info("updatePolicies");

		String config = "Configuration {\r\n"
				+ "  socketPath          = \"/ipc/node.socket\"\r\n"
				+ ", magic               = " + (network.equals("testnet") ? "Just 1097911063" : "Nothing") + "\r\n"
				+ ", epochSlots          = 21600\r\n"
				+ ", addressString       = \"addr_test1qq9prvx8ugwutkwxx9cmmuuajaqmjqwujqlp9d8pvg6gupcvluken35ncjnu0puetf5jvttedkze02d5kf890kquh60slacjyp\"\r\n"
				+ ", verificationKeyFile = \"payment.vkey\"\r\n"
				+ ", signingKeyFile      = \"payment.skey\"\r\n"
				+ "}";
		fileUtil.writeFile("config.mantis", config);

		// @formatter:off
        String[] cmd = new String[] {
                "docker", "run",
                "--rm",
                "-v", cardanoNode.getIpcVolumeName() + ":/ipc",
                "-v", workingDir + ":/work",
                "-w", "/work",
                "wutzebaer/mantis",
                "mantis",
                "chain-scripts",
                "config.mantis",
                "--output", "/work/policy-scripts",
        };
        // @formatter:on
		ProcessUtil.runCommand(cmd);
	}

	private void readPolicies() throws IOException {
		for (File mappingFile : outputPath.listFiles()) {
			if (mappingFile.isFile()) {
				String subject = FilenameUtils.getBaseName(mappingFile.getName());
				if (!policies.containsKey(subject)) {
					String content = Files.readString(mappingFile.toPath());
					policies.put(subject, content);
				}
			}
		}
		log.info("updatePolicies finished: {}", policies.size());
	}

}
