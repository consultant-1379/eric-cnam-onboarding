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

import static com.ericsson.oss.management.onboarding.TestUtils.getResource;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.UNZIP;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.CHARTS_DIR;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.METADATA_YAML;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.VERSION_KEY;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ericsson.oss.management.onboarding.TestUtils;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidInputException;
import com.ericsson.oss.management.onboarding.models.CommandResponse;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = FileServiceImpl.class)
class FileServiceImplTest {

    @Autowired
    private FileServiceImpl fileService;
    @MockBean
    private CommandExecutor commandExecutor;
    @Mock
    private MultipartFile file;

    @TempDir
    private Path folder;

    private static final int TIMEOUT = 1;
    private static final String FILENAME = "spider-app-a-1.0.12-imageless.csar";
    private static final String MOCK_MULTIPART_FILE_NAME = "TempFile";
    private static final String TEMP_FILE_CONTENT = "Content";
    private static final String FILE_NAME = "MyTempFile";
    private static final String TEST_EXCLUDING_CHARTS_DIRECTORY_YAML = "testExcludingChartsDirectory.yaml";
    private static final String EXISTS_ONLY_IN_CHARTS_DIRECTORY_YAML = "existsOnlyInChartsDirectory.yaml";

    @BeforeEach
    void setUp() {
        setField(fileService, "rootDirectory", folder.toString());
    }

    @Test
    void shouldPerformUnzipWithoutAnyExceptions() {
        Path testCsar = Path.of("parent-directory/test-app.csar");
        CommandResponse successesResponse = new CommandResponse("", 0);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(successesResponse);

        assertThatCode(() -> fileService.unzip(testCsar, TIMEOUT))
                .doesNotThrowAnyException();

        String correctCommand = String.format(UNZIP, testCsar, testCsar.getParent());
        verify(commandExecutor).execute(correctCommand, TIMEOUT);
    }

    @Test
    void shouldThrowExceptionIfUnzipCommandPerformsWithNoneZeroStatusCode() {
        Path invalidFile = Path.of("parent-directory/invalid");
        CommandResponse responseWithException = new CommandResponse("Can't unzip", -1);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(responseWithException);

        assertThatThrownBy(() -> fileService.unzip(invalidFile, TIMEOUT))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining(String.format("Failed to unpack file due to: %s",
                                                    responseWithException.getOutput()));
    }

    @Test
    void successfullyGetFileFromDirectory() throws URISyntaxException {
        Path directory = getResource("csar-archives");

        Path result = fileService.getFileFromDirectory(directory, FILENAME);
        assertThat(result.getFileName().toString()).hasToString(FILENAME);
    }

    @Test
    void shouldFailWhenFileNotExists() throws URISyntaxException {
        Path directory = getResource("csar-archives");

        Path result = fileService.getFileFromDirectory(directory, "not-existing.yaml");
        assertThat(result).isNull();
    }

    @Test
    void shouldFailWhenInvalidDirectory() {
        Path directory = Path.of("something");

        assertThatThrownBy(() -> fileService.getFileFromDirectory(directory, FILENAME))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void successfullyUntarArchive() {
        Path archive = Path.of("parent-directory/test-app.tgz");
        CommandResponse successesResponse = new CommandResponse("", 0);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(successesResponse);

        assertThatCode(() -> fileService.untar(archive, archive.getParent(), TIMEOUT))
                .doesNotThrowAnyException();

        verify(commandExecutor).execute(anyString(), eq(TIMEOUT));
    }

    @Test
    void shouldThrowExceptionIfUntarCommandPerformsWithNoneZeroStatusCode() {
        Path invalidFile = Path.of("parent-directory/invalid");
        var parentPath = invalidFile.getParent();
        CommandResponse responseWithException = new CommandResponse("Can't untar", -1);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(responseWithException);

        assertThatThrownBy(() -> fileService.untar(invalidFile, parentPath, TIMEOUT))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining(String.format("Failed to extract archive due to: %s",
                                                    responseWithException.getOutput()));
    }

    @Test
    void successfullyCreateDirectory() {
        Path directory = fileService.createDirectory();
        assertThat(directory)
                .startsWith(Paths.get(System.getProperty("java.io.tmpdir")))
                .isEmptyDirectory();
    }

    @Test
    void failToCreateDirectory() {
        fileService.deleteDirectory(folder);

        assertThatThrownBy(() -> fileService.createDirectory())
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Failed to create directory");
    }

    @Test
    void successfullyDeleteDirectory() {
        Path directory = fileService.createDirectory();
        fileService.deleteDirectory(directory);
        assertThat(directory).doesNotExist();
    }

    @Test
    void successfullyCreateDirectoryWithName() {
        //Test method
        Path directory = fileService.createDirectory("directoryName");

        //Verify
        assertThat(directory)
                .startsWith(Paths.get(System.getProperty("java.io.tmpdir")))
                .isEmptyDirectory();
        assertThat(directory.getFileName().toString()).contains("directoryName");
    }

    @Test
    void failToDeleteDirectory() {
        Path directory = Paths.get("Non-existent");
        assertThatThrownBy(() -> fileService.deleteDirectory(directory))
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Failed to delete directory");
    }

    @Test
    void shouldGetFileOutsideChartDirectory() throws URISyntaxException {
        Path directory = TestUtils.getResource("templates");
        Path fileFromTheDirectory =
                fileService.getFileFromTheDirectoryExcludingLocation(directory, TEST_EXCLUDING_CHARTS_DIRECTORY_YAML, CHARTS_DIR);

        assertThat(fileFromTheDirectory).isNotNull();
        assertThat(fileFromTheDirectory.getFileName().toString()).hasToString(TEST_EXCLUDING_CHARTS_DIRECTORY_YAML);
    }

    @Test
    void shouldNotGetFileFromChartsDirectory() throws URISyntaxException {
        Path directory = TestUtils.getResource("templates");
        Path path = fileService.getFileFromTheDirectoryExcludingLocation(directory, EXISTS_ONLY_IN_CHARTS_DIRECTORY_YAML, CHARTS_DIR);

        assertThat(path).isNull();
    }

    @Test
    void shouldThrowExceptionIfDirectoryIsNotFound() {
        var emptyPath = Path.of("not-found");
        assertThatThrownBy(() -> fileService.getFileFromTheDirectoryExcludingLocation(emptyPath, "not-found", CHARTS_DIR))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void shouldGetFileByNamePartOutsideChartDirectory() throws URISyntaxException {
        Path directory = TestUtils.getResource("templates");
        String filenamePart = "ExcludingChartsDirectory.yaml";
        Path fileFromTheDirectory = fileService.getFileFromTheDirectoryByNamePartExcludingLocation(directory, filenamePart, CHARTS_DIR);

        assertThat(fileFromTheDirectory).isNotNull();
        assertThat(fileFromTheDirectory.getFileName().toString()).hasToString(TEST_EXCLUDING_CHARTS_DIRECTORY_YAML);
    }

    @Test
    void shouldGetFileByNamePartWithFullNameOutsideChartDirectory() throws URISyntaxException {
        Path directory = TestUtils.getResource("templates");
        Path fileFromTheDirectory =
                fileService.getFileFromTheDirectoryByNamePartExcludingLocation(directory, TEST_EXCLUDING_CHARTS_DIRECTORY_YAML, CHARTS_DIR);

        assertThat(fileFromTheDirectory).isNotNull();
        assertThat(fileFromTheDirectory.getFileName().toString()).hasToString(TEST_EXCLUDING_CHARTS_DIRECTORY_YAML);
    }

    @Test
    void shouldNotGetFileFromChartsDirectoryByNamePart() throws URISyntaxException {
        Path directory = TestUtils.getResource("templates");
        Path path = fileService.getFileFromTheDirectoryByNamePartExcludingLocation(directory,
                                                                                   EXISTS_ONLY_IN_CHARTS_DIRECTORY_YAML, CHARTS_DIR);

        assertThat(path).isNull();
    }

    @Test
    void shouldThrowExceptionIfDirectoryIsNotFoundWhenGetFileByNamePart() {
        var emptyPath = Path.of("not-found");
        assertThatThrownBy(() -> fileService
                .getFileFromTheDirectoryByNamePartExcludingLocation(emptyPath, "not-found", CHARTS_DIR))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void shouldGetValueByProperty() throws URISyntaxException {
        String expectedValue = "3.6.0+14";
        Path resource = getResource(METADATA_YAML);
        String actualValue = fileService.getValueByPropertyFromFile(resource, VERSION_KEY);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    void shouldThrowExceptionWhenFileIsNotFound() {
        Path nonExistentFile = Path.of("nonExistentFile.yaml");

        assertThatThrownBy(() -> fileService.getValueByPropertyFromFile(nonExistentFile, VERSION_KEY))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Unable to parse the yaml file");
    }

    @Test
    void shouldThrowExceptionWhenPropertyIsNotFound() throws URISyntaxException {
        String propertyName = "notFound";
        Path resource = getResource(METADATA_YAML);
        assertThatThrownBy(() -> fileService.getValueByPropertyFromFile(resource, propertyName))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining(String.format("File %s must contain %s", resource, propertyName));
    }

    @Test
    void successfullyStoreFile() {
        MockMultipartFile file = new MockMultipartFile(MOCK_MULTIPART_FILE_NAME, TEMP_FILE_CONTENT.getBytes());
        Path directory = fileService.createDirectory();
        Path storedFile = fileService.storeFileIn(directory, file, FILE_NAME);
        assertThat(storedFile)
                .exists()
                .hasContent(TEMP_FILE_CONTENT);
        assertThat(directory).isDirectoryContaining(path -> path.getFileName().toString().equalsIgnoreCase(FILE_NAME));
        fileService.deleteDirectory(directory);
    }

    @Test
    void failToStoreFile() {
        MockMultipartFile file = new MockMultipartFile(MOCK_MULTIPART_FILE_NAME, TEMP_FILE_CONTENT.getBytes());
        var nonExistentPath = Paths.get("non-Existent");
        assertThatThrownBy(() -> fileService.storeFileIn(nonExistentPath, file, "failed"))
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Failed to store file");
    }

    @Test
    void convertFileToPathSuccessfully() {
        when(file.getOriginalFilename()).thenReturn(MOCK_MULTIPART_FILE_NAME);
        Path result = fileService.convertToPath(file);

        assertThat(result).isNotNull();
        assertThat(result.getFileName().toString()).contains(MOCK_MULTIPART_FILE_NAME);
        fileService.deleteDirectory(result.getParent());
    }

    @Test
    void shouldFailWhenFileIsMissed() {
        assertThatThrownBy(() -> fileService.convertToPath(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("The file is missing from the request");
    }

    @Test
    void shouldReadFileSuccessfully() throws URISyntaxException {
        Path file = getResource("manifest.json");
        JSONArray result = fileService.readFile(file);

        assertThat(result).isNotNull();
        assertThat(result.toString()).hasToString("[{\"RepoTags\":[\"armdocker.rnd.ericsson.se\\/dockerhub-ericsson-remote\\/busybox:1.32.0\"],"
                                                        + "\"Config\":\"219ee5171f8006d1462fa76c12b9b01ab672dbc8b283f186841bf2c3ca8e3c93.json\","
                                                        + "\"Layers\":[\"5b4ef804af565ff504bf7693239e5f26b4782484f068226b7244117760603d0d\\/layer"
                                                        + ".tar\"]}]");
    }

    @Test
    void shouldFailReadFileWhenFileNotExist() {
        Path file = Path.of("not-exist.json");
        assertThatThrownBy(() -> fileService.readFile(file))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("file does not exist");
    }

    @Test
    void shouldFailReadNotCorrectFile() throws URISyntaxException {
        Path file = getResource(METADATA_YAML);
        assertThatThrownBy(() -> fileService.readFile(file))
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Unable to read file");
    }

    @Test
    void shouldVerifyTrueIfDirectoryExists() {
        //Init
        Path directory = fileService.createDirectory("directoryName");

        //Test method
        boolean result = fileService.verifyDirectoryExist(directory);

        //Verify
        assertThat(result).isTrue();
    }

}
