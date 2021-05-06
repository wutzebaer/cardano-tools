package de.peterspace.cardanotools.cardano;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.codec.binary.Base16;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.process.ProcessUtil;
import de.peterspace.cardanotools.rest.dto.TokenRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class TokenRegistry {

	@Value("${working.dir}")
	private String workingDir;

	private final FileUtil fileUtil;

	public TokenRegistration createTokenRegistration(RegistrationMetadata registrationMetadata) throws Exception {
		String subject = registrationMetadata.getPolicyId() + encodeBase16(registrationMetadata.getAssetName());
		initDraft(subject);
		addRequiredFields(subject, registrationMetadata);
		addOptionalFields(subject, registrationMetadata);
		sign(subject, registrationMetadata);
		finalize(subject, registrationMetadata);
		return new TokenRegistration(subject + ".json", fileUtil.consumeFile(subject + ".json"));
	}

	private void initDraft(String subject) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");

		cmd.add("run");
		cmd.add("--rm");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-w");
		cmd.add("/work");

		cmd.add("wutzebaer/cardano-tools-token-metadata-creator");
		cmd.add("token-metadata-creator");
		cmd.add("entry");

		cmd.add("--init");
		cmd.add(subject);

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}

	private void addRequiredFields(String subject, RegistrationMetadata registrationMetadata) throws Exception {
		String temporaryFilePrefix = UUID.randomUUID().toString();
		fileUtil.writeFile(temporaryFilePrefix + ".script", registrationMetadata.getPolicy());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");

		cmd.add("run");
		cmd.add("--rm");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-w");
		cmd.add("/work");

		cmd.add("wutzebaer/cardano-tools-token-metadata-creator");
		cmd.add("token-metadata-creator");

		cmd.add("entry");
		cmd.add(subject);

		cmd.add("--name");
		cmd.add(registrationMetadata.getName());

		cmd.add("--description");
		cmd.add(registrationMetadata.getDescription());

		cmd.add("--policy");
		cmd.add(temporaryFilePrefix + ".script");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		fileUtil.removeFile(temporaryFilePrefix + ".script");
	}

	private void addOptionalFields(String subject, RegistrationMetadata registrationMetadata) throws Exception {
		String temporaryFilePrefix = UUID.randomUUID().toString();
		fileUtil.writeFile(temporaryFilePrefix + ".script", registrationMetadata.getPolicy());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");

		cmd.add("run");
		cmd.add("--rm");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-w");
		cmd.add("/work");

		cmd.add("wutzebaer/cardano-tools-token-metadata-creator");
		cmd.add("token-metadata-creator");

		cmd.add("entry");
		cmd.add(subject);

		if (!StringUtils.isBlank(registrationMetadata.getTicker())) {
			cmd.add("--ticker");
			cmd.add(registrationMetadata.getTicker());
		}

		if (!StringUtils.isBlank(registrationMetadata.getUrl())) {
			cmd.add("--url");
			cmd.add(registrationMetadata.getUrl());
		}

		if (registrationMetadata.getLogo() != null) {
			fileUtil.writeFile(temporaryFilePrefix + ".png", registrationMetadata.getLogo());
			cmd.add("--logo");
			cmd.add(temporaryFilePrefix + ".png");
		}

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		fileUtil.removeFile(temporaryFilePrefix + ".script");
		if (registrationMetadata.getLogo() != null) {
			fileUtil.removeFile(temporaryFilePrefix + ".png");
		}
	}

	private void sign(String subject, RegistrationMetadata registrationMetadata) throws Exception {
		String temporaryFilePrefix = UUID.randomUUID().toString();
		fileUtil.writeFile(temporaryFilePrefix + ".skey", registrationMetadata.getPolicySkey());

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");

		cmd.add("run");
		cmd.add("--rm");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-w");
		cmd.add("/work");

		cmd.add("wutzebaer/cardano-tools-token-metadata-creator");
		cmd.add("token-metadata-creator");

		cmd.add("entry");
		cmd.add(subject);

		cmd.add("-a");
		cmd.add(temporaryFilePrefix + ".skey");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		fileUtil.removeFile(temporaryFilePrefix + ".skey");
	}

	private void finalize(String subject, RegistrationMetadata registrationMetadata) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("docker");

		cmd.add("run");
		cmd.add("--rm");

		cmd.add("-v");
		cmd.add(workingDir + ":/work");

		cmd.add("-w");
		cmd.add("/work");

		cmd.add("wutzebaer/cardano-tools-token-metadata-creator");
		cmd.add("token-metadata-creator");

		cmd.add("entry");
		cmd.add(subject);

		cmd.add("--finalize");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));
	}

	private static String encodeBase16(String content) {
		return new String(new Base16().encode(content.getBytes()));
	}

}
