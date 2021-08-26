package de.peterspace.cardanotools.cardano;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProjectRegistry {

	private final TaskExecutor taskExecutor;

	@Getter
	private Map<String, ProjectMetadata> projectRegistryMetadata;

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
		projectRegistryMetadata = fetchRegistry();
	}

	@Data
	public static class ProjectMetadata {
		private String project;
		private List<String> tags;
		private List<String> policies;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Map<String, ProjectMetadata> fetchRegistry() throws Exception {

		final Map<String, ProjectMetadata> result = new HashMap<>();
		ZipInputStream zis = new ZipInputStream(new URL("https://github.com/Cardano-NFTs/policyIDs/archive/refs/heads/main.zip").openStream());
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}

			log.info("Reading {}", ze.getName());
			try {

				String jsonString = IOUtils.toString(zis, StandardCharsets.UTF_8);
				if (jsonString.trim().startsWith("{")) {
					JSONObject jsonObject = new JSONObject(jsonString);
					processJsonObject(result, jsonObject);
				} else {
					JSONArray jsonArray = new JSONArray(jsonString);
					for (int i = 0; i < jsonArray.length(); i++) {
						processJsonObject(result, jsonArray.getJSONObject(i));
					}
				}
			} catch (Exception e) {
				log.error("File {} failed: {}", ze.getName(), e.getMessage());
			}
		}

		return result;
	}

	private void processJsonObject(final Map<String, ProjectMetadata> result, JSONObject jsonObject) {
		ProjectMetadata projectMetadata = new ProjectMetadata();
		projectMetadata.setProject(jsonObject.getString("project"));

		if (jsonObject.has("tags")) {
			projectMetadata.setTags(jsonStringArrayToList(jsonObject.getJSONArray("tags")));
		} else {
			projectMetadata.setTags(List.of());
		}
		projectMetadata.setPolicies(jsonStringArrayToList(jsonObject.getJSONArray("policies")));

		for (String policy : projectMetadata.getPolicies()) {
			result.put(policy, projectMetadata);
		}
	}

	private List<String> jsonStringArrayToList(JSONArray jsonList) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < jsonList.length(); i++) {
			list.add(jsonList.getString(i));
		}
		return list;
	}

}
