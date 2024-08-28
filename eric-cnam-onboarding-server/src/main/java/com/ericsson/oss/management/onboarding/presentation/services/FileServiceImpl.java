/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.management.onboarding.presentation.services;

import static java.util.Comparator.reverseOrder;

import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.UNTAR;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.UNZIP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.management.onboarding.presentation.exceptions.ZipCompressedException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.AbstractFileHeader;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.ericsson.oss.management.onboarding.models.CommandResponse;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidInputException;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;
import com.ericsson.oss.management.onboarding.utils.YAMLParseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final CommandExecutor commandExecutor;

    @Value("${directory.root}")
    private String rootDirectory;

    @Override
    public void unzip(Path pathToUnpack, int timeout) {
        var uncompressedSize = getUncompressedSize(pathToUnpack);
        var freeSpace = getFreeSpace(pathToUnpack);

        if (uncompressedSize > freeSpace) {
            log.error(String.format("Can't unpack archive to path %s. Uncompress archive contain %s bytes," +
                            " amount free space to current path %s bytes", pathToUnpack, uncompressedSize, freeSpace));
            deleteDirectory(pathToUnpack.getParent());
            throw new ZipCompressedException(String.format("There is not enough space to unpack the CSAR: %s", pathToUnpack.getFileName()));
        }

        var command = String.format(UNZIP, pathToUnpack, pathToUnpack.getParent().toString());
        CommandResponse response = commandExecutor.execute(command, timeout);
        if (response.getExitCode() != 0) {
            log.error("Failed to unpack file. See for details in response: {}", response.getOutput());
            throw new InvalidFileException(String.format("Failed to unpack file due to: %s", response.getOutput()));
        }
    }

    private long getFreeSpace(Path pathToUnpack) {
        return new File(pathToUnpack.toString()).getFreeSpace();
    }

    private long getUncompressedSize(Path pathToUnpack) {
        try (var zipFile = new ZipFile(pathToUnpack.toString())) {
            return zipFile.getFileHeaders().stream()
                    .map(AbstractFileHeader::getUncompressedSize)
                    .mapToLong(Long::longValue)
                    .sum();
        } catch (IOException ex) {
            throw new InvalidFileException(String.format("Failed to get uncompressed size of the archive: %s. Details %s",
                    pathToUnpack.getFileName(), ex.getMessage()));
        }
    }

    @Override
    public Path untar(Path archive, Path directory, int timeout) {
        var command = String.format(UNTAR, archive, directory);
        CommandResponse response = commandExecutor.execute(command, timeout);
        if (response.getExitCode() != 0) {
            throw new InvalidFileException(String.format("Failed to extract archive due to: %s", response.getOutput()));
        }
        return directory;
    }

    @Override
    public Path createDirectory() {
        return createDirectory(UUID.randomUUID().toString());
    }

    @Override
    public Path createDirectory(String directoryName) {
        log.info(String.format("Creating directory with the given name %s", directoryName));
        try {
            var directory = Paths.get(rootDirectory).resolve(directoryName);
            return Files.createDirectory(directory);
        } catch (IOException e) {
            throw new InternalRuntimeException(String.format("Failed to create directory with name %s. Details: %s",
                                                             directoryName, e.getMessage()));
        }
    }

    @Override
    public Path createDirectory(Path path) {
        log.info(String.format("Creating directory %s", path.toString()));
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new InternalRuntimeException(String.format("Failed to create directory  %s. Details: %s", path.getFileName(), e.getMessage()));
        }
    }

    @Override
    public void deleteDirectory(final Path directory) {
        log.info("Will delete {}", directory);
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(reverseOrder()).forEach(this::deleteFile);
        } catch (IOException e) {
            throw new InternalRuntimeException("Failed to delete directory. Details: " + e.getMessage());
        }
    }

    @Override
    public Path getFileFromDirectory(Path directory, String filename) {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.map(Path::toAbsolutePath)
                    .filter(fileName -> fileName.toString().endsWith(filename))
                    .findAny().orElse(null);
        } catch (IOException e) {
            throw new InvalidFileException(
                    String.format("Something went wrong during searching file %s in the directory %s", filename,
                                  directory.toAbsolutePath()));
        }
    }

    @Override
    public Path getFileFromTheDirectoryExcludingLocation(Path directory, String fileName,
                                                         String directoryToExclude) {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk.filter(item -> item.getFileName().toString().equals(fileName))
                    .filter(item -> !item.toString().contains(directoryToExclude))
                    .findAny().orElse(null);
        } catch (IOException e) {
            throw new InvalidFileException(e.getMessage());
        }
    }

    @Override
    public Path getFileFromTheDirectoryByNamePartExcludingLocation(Path directory, String fileNamePart,
                                                                   String directoryToExclude) {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk.filter(item -> item.getFileName().toString().contains(fileNamePart))
                    .filter(item -> !item.toString().contains(directoryToExclude))
                    .findAny().orElse(null);
        } catch (IOException e) {
            throw new InvalidFileException(e.getMessage());
        }
    }

    @Override
    public String getValueByPropertyFromFile(Path file, String propertyName) {
        try (var inputStream = Files.newInputStream(file)) {
            Iterable<Object> content = YAMLParseUtils.getYamlContent(inputStream);
            return getValueFromContent(content, file, propertyName);
        } catch (Exception e) {
            throw new InvalidFileException("Unable to parse the yaml file. Details: " + e.getMessage());
        }
    }

    @Override
    public Path convertToPath(MultipartFile file) {
        log.info("Storing the file {}", file);
        Path directory = createDirectory();
        return Optional.ofNullable(file)
                .map(item -> storeFileIn(directory, file, file.getOriginalFilename()))
                .orElseGet(() -> {
                    deleteDirectory(directory);
                    throw new InvalidInputException("The file is missing from the request");
                });
    }

    @Override
    public Path storeFileIn(Path directory, MultipartFile file, String filename) {
        log.info("Storing {} in {}", filename, directory);
        var destination = directory.resolve(filename);
        try {
            file.transferTo(destination);
        } catch (IOException e) {
            throw new InternalRuntimeException("Failed to store file due to " + e.getMessage());
        }
        return destination;
    }

    @Override
    public JSONArray readFile(Path filePath) {
        if (filePath.toFile().exists()) {
            try {
                log.info("Reading the content of file");
                var contents = new String(Files.readAllBytes(filePath));
                return new JSONArray(contents);
            } catch (Exception e) {
                throw new InternalRuntimeException("Unable to read file. Details: " + e.getMessage());
            }
        } else {
            throw new InvalidInputException(String.format("%s file does not exist", filePath));
        }
    }

    @Override
    public boolean verifyDirectoryExist(Path directory) {
        return Files.exists(directory);
    }

    private String getValueFromContent(Iterable<Object> content, Path file, String propertyName) {
        try {
            String resultValue = null;
            for (Object document : content) {
                Map<String, Object> mappedDocument = (Map) document;
                Object value = Optional.ofNullable(mappedDocument.get(propertyName))
                        .orElseThrow(() -> new InvalidFileException(String.format("File %s must contain %s", file.toString(), propertyName)));
                resultValue = value.toString();
            }
            return resultValue;
        } catch (ScannerException se) {
            throw new InvalidFileException(se.getMessage());
        }
    }

    private void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new InvalidFileException("Failed to delete file. Details: " + e.getMessage());
        }
    }

    @Override
    public String readDataFromFile(final Path file) {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new InvalidFileException(String.format(
                    "Can't read file %s. Details: %s", file.getFileName().toString(), e.getMessage()));
        }
    }

    @Override
    public byte[] readAsBytes(final MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse the yaml file to byte array", e);
        }
    }
}
