package de.peterspace.cardanotools.cardano;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class FileUtil {

	@Value("${working.dir}")
	private String workingDir;

	public String consumeFile(String filename) throws Exception {
		Path path = Paths.get(workingDir, filename);
		String readString = Files.readString(path);
		Files.delete(path);
		return readString;
	}

	public String readFile(String filename) throws Exception {
		return Files.readString(Paths.get(workingDir, filename));
	}

	public void writeFile(String filename, String content) throws Exception {
		Files.writeString(Paths.get(workingDir, filename), content);
	}

	public void writeFile(String filename, byte[] content) throws Exception {
		Files.write(Paths.get(workingDir, filename), content);
	}

	public void removeFile(String filename) throws Exception {
		Files.delete(Paths.get(workingDir, filename));
	}

}
