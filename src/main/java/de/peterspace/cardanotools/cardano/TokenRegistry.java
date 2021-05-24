package de.peterspace.cardanotools.cardano;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base16;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

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

	@Value("${github.username}")
	private String githubUsername;

	@Value("${github.apitoken}")
	private String githubApitoken;

	@Value("${github.registry.fork}")
	private String githubRegistryFork;

	private final FileUtil fileUtil;

	@PostConstruct
	public void init() throws Exception {
		File repoDir = Paths.get(workingDir, "cardano-token-registry").toFile();
		if (repoDir.exists()) {
			Git git = Git.open(repoDir);
			git
					.fetch()
					.call();
		} else {
			Git.cloneRepository()
					.setURI(githubRegistryFork)
					.setDirectory(repoDir)
					.call();
		}
	}

	@lombok.Value
	public static class PullRequest {
		String title;
		String head;
		String base;
	}

	public String createTokenRegistration(RegistrationMetadata registrationMetadata) throws Exception {
		String subject = registrationMetadata.getPolicyId() + encodeBase16(registrationMetadata.getAssetName());
		initDraft(subject);
		addRequiredFields(subject, registrationMetadata);
		addOptionalFields(subject, registrationMetadata);
		sign(subject, registrationMetadata);
		finalize(subject, registrationMetadata);

		String branchname = pushToFork(registrationMetadata.getName(), subject + ".json", fileUtil.readFileBinary(subject + ".json"));
		fileUtil.removeFile(subject + ".json");

		String url = createPullRequest(branchname, registrationMetadata.getName());

		return url;
	}

	public String pushToFork(String tokenname, String filename, byte[] data) throws Exception {

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

	public String createPullRequest(String branchname, String title) {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(githubUsername, githubApitoken).build();
		String result = restTemplate.postForObject("https://api.github.com/repos/cardano-foundation/cardano-token-registry/pulls", new PullRequest(title, "cardano-tools-nft:" + branchname, "master"), String.class);
		return new JSONObject(result).getString("url");
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
