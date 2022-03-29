package de.peterspace.cardanotools.cardano;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import de.peterspace.cardanotools.model.RegistrationMetadata;
import de.peterspace.cardanotools.process.ProcessUtil;
import de.peterspace.cardanotools.repository.RegistrationMetadataRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class TokenRegistry {

	@Value("${working.dir}")
	private String workingDir;

	@Value("${github.username}")
	private String githubUsername;

	@Value("${github.apitoken}")
	private String githubApitoken;

	@Value("${github.registry.fork}")
	private String githubRegistryFork;

	private final FileUtil fileUtil;
	private final RegistrationMetadataRepository registrationMetadataRepository;
	private final TaskExecutor taskExecutor;

	@Getter
	private Map<String, TokenRegistryMetadata> tokenRegistryMetadata;

	@PostConstruct
	public void init() throws Exception {
		taskExecutor.execute(() -> {
			try {
				updateRegistry();
			} catch (Exception e) {
				log.error("Init TokenRegistryMetadata", e);
			}
		});

	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateRegistry() throws Exception {
		fetchRegistry();
	}

	@lombok.Value
	public static class PullRequest {
		String title;
		String head;
		String base;
		String body;
	}

	@Data
	public static class TokenRegistryMetadata {
		@NotBlank
		String name;
		@NotBlank
		String description;
		String ticker;
		String url;
		String logo;
	}

	public String createTokenRegistration(RegistrationMetadata registrationMetadata) throws Exception {

		if (registrationMetadataRepository.existsByPolicyIdAndAssetName(registrationMetadata.getPolicyId(), registrationMetadata.getAssetName())) {
			throw new Exception("Your token is already registered!");
		}

		String subject = CardanoUtil.createSubject(registrationMetadata.getPolicyId(), registrationMetadata.getAssetName());
		addRequiredFields(subject, registrationMetadata);

		try {
			registrationMetadataRepository.save(registrationMetadata);
		} catch (Exception e) {
			throw new Exception("Your token is already registered!", e);
		}

		String branchname = pushToFork(registrationMetadata.getName(), subject + ".json", fileUtil.readFileBinary(subject + ".json"));
		fileUtil.removeFile(subject + ".json");

		String url = createPullRequest(branchname, registrationMetadata.getName());
		return url;
	}

	private void fetchRegistry() throws Exception {

		final Map<String, TokenRegistryMetadata> result = new HashMap<>();
		ZipInputStream zis = new ZipInputStream(new URL("https://github.com/cardano-foundation/cardano-token-registry/archive/refs/heads/master.zip").openStream());
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}
			if (!ze.getName().startsWith("cardano-token-registry-master/mappings")) {
				continue;
			}

			log.trace("Reading {}", ze.getName());

			try {
				String subject = FilenameUtils.getBaseName(ze.getName());
				String content = IOUtils.toString(zis, StandardCharsets.UTF_8);
				JSONObject jsonObject = new JSONObject(content);
				TokenRegistryMetadata tokenRegistryMetadata = new TokenRegistryMetadata();
				tokenRegistryMetadata.setName(jsonObject.getJSONObject("name").getString("value"));
				tokenRegistryMetadata.setDescription(jsonObject.getJSONObject("description").getString("value"));
				if (jsonObject.has("ticker"))
					tokenRegistryMetadata.setTicker(jsonObject.getJSONObject("ticker").getString("value"));
				if (jsonObject.has("url"))
					tokenRegistryMetadata.setUrl(jsonObject.getJSONObject("url").getString("value"));
				if (jsonObject.has("logo"))
					tokenRegistryMetadata.setLogo(jsonObject.getJSONObject("logo").getString("value"));
				result.put(subject, tokenRegistryMetadata);
			} catch (Exception e) {
				log.error("File {} failed: {}", ze.getName(), e.getMessage());
			}
		}

		tokenRegistryMetadata = result;

		log.info("Fetched {} tokens from cardano-token-registry", result.size());
	}

	private String pushToFork(String tokenname, String filename, byte[] data) throws Exception {

		final String branchname = UUID.randomUUID().toString();
		final String githubRegistryFork = "https://github.com/cardano-tools-nft/cardano-token-registry.git";
		final File tempDir = Files.createTempDirectory("cardano-token-registry").toFile();

		try (Git git = Git.cloneRepository().setURI(githubRegistryFork).setDirectory(tempDir).call()) {
			Files.write(Paths.get(tempDir.getAbsolutePath(), "mappings", filename), data);
			git.add()
					.addFilepattern("mappings/" + filename)
					.call();
			git.commit()
					.setMessage(tokenname)
					.setAuthor(branchname, githubRegistryFork)
					.call();
			git.push()
					.setRefSpecs(new RefSpec("master:" + branchname))
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubUsername, githubApitoken))
					.call();
		}

		return branchname;
	}

	private String createPullRequest(String branchname, String title) {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(githubUsername, githubApitoken).build();
		PullRequest pullRequest = new PullRequest(title, "cardano-tools-nft:" + branchname, "master", "# Pull Request Template\r\n"
				+ "\r\n"
				+ "## Description\r\n"
				+ "\r\n"
				+ "Please include a short summary of the changes in this PR.\r\n"
				+ "\r\n"
				+ "## Type of change\r\n"
				+ "\r\n"
				+ "- [x] Metadata related change\r\n"
				+ "- [ ] Other\r\n"
				+ "\r\n"
				+ "## Checklist:\r\n"
				+ "\r\n"
				+ "- [ ] For metadata related changes, this PR code passes the Github Actions metadata validation\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "## Metadata PRs\r\n"
				+ "\r\n"
				+ "Please note it may take up to 4 hours for merged changes to take effect on the metadata server.\r\n"
				+ "");
		String result = restTemplate.postForObject("https://api.github.com/repos/cardano-foundation/cardano-token-registry/pulls", pullRequest, String.class);
		return new JSONObject(result).getString("html_url");
	}


	private void addRequiredFields(String subject, RegistrationMetadata registrationMetadata) throws Exception {
		String temporaryFilePrefix = UUID.randomUUID().toString();
		fileUtil.writeFile(temporaryFilePrefix + ".script", registrationMetadata.getPolicy());
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

		cmd.add("--init");
		cmd.add(subject);

		cmd.add("--name");
		cmd.add(registrationMetadata.getName());

		cmd.add("--description");
		cmd.add(registrationMetadata.getDescription());

		cmd.add("--policy");
		cmd.add(temporaryFilePrefix + ".script");

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

		cmd.add("-a");
		cmd.add(temporaryFilePrefix + ".skey");

		cmd.add("--finalize");

		ProcessUtil.runCommand(cmd.toArray(new String[0]));

		fileUtil.removeFile(temporaryFilePrefix + ".script");
		fileUtil.removeFile(temporaryFilePrefix + ".skey");
		if (registrationMetadata.getLogo() != null) {
			fileUtil.removeFile(temporaryFilePrefix + ".png");
		}
	}




	public static void mainBAK(String[] args) throws Exception {
		final String branchname = UUID.randomUUID().toString();
		InMemoryRepository repo = new InMemoryRepository(new DfsRepositoryDescription());

		try (Git git = new Git(repo)) {

			git.fetch()
					.setRemote("https://github.com/cardano-tools-nft/cardano-token-registry.git")
					.setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
					.call();

			ObjectInserter objectInserter = repo.newObjectInserter();
			ObjectId blobId = objectInserter.insert(Constants.OBJ_BLOB, "Hello".getBytes());
			objectInserter.flush();

			TreeFormatter subtreeFormatter = new TreeFormatter();
			subtreeFormatter.append("coolfile.json", FileMode.REGULAR_FILE, blobId);
			ObjectId subtreeId = objectInserter.insert(subtreeFormatter);
			objectInserter.flush();

			TreeFormatter treeFormatter = new TreeFormatter();
			treeFormatter.append("mappingsgg", FileMode.TREE, subtreeId);

			ObjectId lastCommitId = repo.resolve("refs/heads/master");
			RevWalk revWalk = new RevWalk(repo);
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repo);
			treeWalk.addTree(tree);
			while (treeWalk.next()) {
				log.debug(treeWalk.getNameString() + " " + treeWalk.getFileMode());
				treeFormatter.append(treeWalk.getNameString(), treeWalk.getFileMode(), treeWalk.getObjectId(0));
			}

			ObjectId treeId = objectInserter.insert(treeFormatter);
			objectInserter.flush();

			CommitBuilder commitBuilder = new CommitBuilder();
			commitBuilder.setTreeId(treeId);
			commitBuilder.setMessage("MESSAGE");
			PersonIdent person = new PersonIdent("cardano-tools-nft", "cardano-tools-nft@email.de");
			commitBuilder.setAuthor(person);
			commitBuilder.setCommitter(person);
			commitBuilder.setParentId(repo.resolve("master"));
			ObjectId commitId = objectInserter.insert(commitBuilder);
			objectInserter.flush();

			git.branchCreate()
					.setName(branchname)
					.setStartPoint(ObjectId.toString(commitId))
					.call();

			git.push()
					.setRemote("https://github.com/cardano-tools-nft/cardano-token-registry.git")
					.setRefSpecs(new RefSpec(branchname))
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("cardano-tools-nft", "PX0oKHRsRrorQ0zGrRIg"))
					.setForce(true)
					.call();

		}
	}
}
