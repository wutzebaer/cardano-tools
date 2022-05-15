package de.peterspace.cardanotools.cardano;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
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

	@Value("${cardano-node.ipc-volume-name}")
	private String ipcVolumeName;

	private final FileUtil fileUtil;
	private final CardanoNode cardanoNode;
	private final TaskExecutor taskExecutor;
	private File outputPath;

	@Getter
	private final Map<String, String> policies = new HashMap<>();

	@PostConstruct
	public void init() throws Exception {
		outputPath = new File(workingDir, "policy-scripts");
		outputPath.mkdirs();

		taskExecutor.execute(() -> {
			try {
				if (outputPath.listFiles().length == 0) {
					extractPolicies();
				}
				readPolicies();
			} catch (Exception e) {
				log.error("Create json index", e);
			}
		});

	}

	@Scheduled(cron = "${policy.cron}")
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
                "-v", ipcVolumeName + ":/ipc",
                "-v", workingDir + ":/work",
                "-w", "/work",
                "wutzebaer/mantis",
                "mantra",
                "chain-scripts",
                "config.mantis",
                "--output", "/work/policy-scripts",
        };
        // @formatter:on
		ProcessUtil.runCommand(cmd);
		log.info("Extracted {} policies from chain", outputPath.listFiles().length);
	}

	private void readPolicies() throws IOException {
		File[] files = outputPath.listFiles();
		long count = 0;
		for (File mappingFile : files) {
			count++;
			if (mappingFile.isFile()) {
				String subject = FilenameUtils.getBaseName(mappingFile.getName());
				if (!policies.containsKey(subject)) {
					String content = Files.readString(mappingFile.toPath());
					policies.put(subject, content);
				}
				float progress = (float) count / files.length * 100;
				if (count % 2000 == 0 || count == files.length) {
					log.trace("updatePolicies read: {}", String.format("%.2f%%", progress));
				}
			}
		}
		log.info("Read {} policies from files", policies.size());
	}

}
