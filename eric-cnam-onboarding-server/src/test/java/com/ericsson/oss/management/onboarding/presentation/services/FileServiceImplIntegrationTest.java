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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {FileServiceImpl.class, CommandExecutor.class})
class FileServiceImplIntegrationTest {

    @Autowired
    private FileServiceImpl fileService;

    @TempDir
    private Path tempDir;

    private static final Path TEST_CSAR_ARCHIVE = Path.of("src/test/resources/csar-archives/spider-app-a-1.0.12-imageless.csar");
    private static final Path TGZ_ARCHIVE = Path.of("src/test/resources/helmsource-1.2.3-4.tgz");
    private static final int TIMEOUT = 1;

    @Test
    void shouldUnzipArchiveToParentDirectory() throws IOException {
        Path csarArchive = Files.copy(TEST_CSAR_ARCHIVE, tempDir.resolve(TEST_CSAR_ARCHIVE.getFileName()));

        fileService.unzip(csarArchive, TIMEOUT);

        assertThat(tempDir.toFile())
                .isNotEmptyDirectory()
                .isDirectoryContaining(file -> file.getName().equals("spider-app-a.mf"))
                .isDirectoryContaining(file -> file.getName().equals("TOSCA-Metadata"))
                .isDirectoryContaining(file -> file.getName().equals("Files"))
                .isDirectoryContaining(file -> file.getName().equals("Definitions"));
    }

    @Test
    void shouldThrowExceptionIfUnzipCommandNotPerformWithExitCodeZero() throws IOException {
        Path invalidFile = Files.createFile(tempDir.resolve("invalid.txt"));

        assertThatThrownBy(() -> fileService.unzip(invalidFile, TIMEOUT))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Failed to unpack file due to:");
    }

    @Test
    void shouldUntarArchiveToNewDirectory() throws IOException {
        Path csarArchive = Files.copy(TGZ_ARCHIVE, tempDir.resolve(TGZ_ARCHIVE.getFileName()));

        Path result = fileService.untar(csarArchive, csarArchive.getParent(), TIMEOUT);

        assertThat(result.toFile())
                .isNotEmptyDirectory()
                .isDirectoryContaining(file -> file.getName().equals("helmfile.yaml"));
        fileService.deleteDirectory(result);
    }

    @Test
    void shouldThrowExceptionIfUntarCommandNotPerformWithExitCodeZero() throws IOException {
        Path invalidFile = Files.createFile(tempDir.resolve("invalid.txt"));
        var parentPath = invalidFile.getParent();

        assertThatThrownBy(() -> fileService.untar(invalidFile, parentPath, TIMEOUT))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Failed to extract archive due to:");
    }

}
