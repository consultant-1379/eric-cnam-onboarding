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

package com.ericsson.oss.management.onboarding.presentation.services.workloadinstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_GIT_PATH;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.HELM_SOURCE_URL;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.NAMESPACE;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.WORKLOAD_INSTANCE_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePutRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;
import com.ericsson.oss.management.onboarding.presentation.exceptions.GitRepoConnectionException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.presentation.exceptions.NotUniqueWorkloadInstanceException;
import com.ericsson.oss.management.onboarding.presentation.repositories.FileRepository;
import com.ericsson.oss.management.onboarding.presentation.repositories.GitRepoRepository;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.FileServiceImpl;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

@ActiveProfiles("test")
@SpringBootTest(classes = {WorkloadInstanceServiceImpl.class, FileRepository.class, FileServiceImpl.class, CommandExecutor.class})
@TestPropertySource(properties = {"gitrepo.url=/local/tmp/LocalGitRepo"})
class WorkloadInstanceServiceImpTest {

    private static final String EXPECTED_RESULT = "/local/tmp/LocalGitRepo/workloadinstancename";
    private static final String TEST_SOURCE_URL = "test/url";

    @Autowired
    private WorkloadInstanceServiceImpl instanceService;
    @MockBean
    private GitRepoRepository gitRepoRepository;
    @SpyBean
    private FileService fileService;
    @MockBean
    private FileRepository fileRepository;
    @Mock
    private MultipartFile values;
    @Mock
    private MultipartFile clusterConnectionInfo;
    @MockBean
    private GitAPIException gitAPIException;
    @Value("${directory.root}")
    private String rootDirectory;

    @Test
    void shouldReturnAcceptedWhenWorkloadInstanceCreateWithDto() throws GitAPIException {
        //Init
        doNothing().when(gitRepoRepository).cloneRepository(anyString());
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();

        //Test method
        instanceService.create(dto, null, null);

        //Verify
        verify(fileRepository).save(any(), any(WorkloadInstancePostRequestDto.class));
        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldReturnAcceptedWhenWorkloadInstanceUpdateWithDto() throws GitAPIException, IOException {
        //Init
        doNothing().when(gitRepoRepository).pull(anyString());
        Map<String, Object> additionParameters = Map.of("testKey", "testValue");
        var dto = setWorkloadInstancePutRequestDto(TEST_SOURCE_URL, additionParameters);
        Path directory = createDirectory(dto);
        prepareFile(dto, directory);

        //Test method
        var result = instanceService.update(dto, null);

        //Verify
        assertThat(result.getUrl()).isEqualTo(EXPECTED_RESULT);

        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldFailWhenDirectoryWithTheSameWorkloadInstanceNameAlreadyPresent() {
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        when(fileRepository.existsByWorkloadInstanceName(anyString())).thenReturn(true);

        assertThatThrownBy(() -> instanceService.create(dto, values, clusterConnectionInfo))
                .isInstanceOf(NotUniqueWorkloadInstanceException.class);
    }

    @Test
    void shouldReturnAcceptedWhenWorkloadInstanceCreateWithAllParameters() {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();

        //Test method
        instanceService.create(dto, values, clusterConnectionInfo);

        //Verify
        verify(fileRepository).save(any(), any(WorkloadInstancePostRequestDto.class));
        verify(fileRepository, times(2)).save(any(), (MultipartFile) any());
        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldSuccessfullyCloneLocalGitRepo() throws GitAPIException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();

        //Test method
        WorkloadInstanceResponseDto result = instanceService.create(dto, values, clusterConnectionInfo);

        //Verify
        assertThat(result.getUrl()).isEqualTo(EXPECTED_RESULT);

        verify(gitRepoRepository).cloneRepository(any());
        verify(fileService).createDirectory(any(Path.class));
        verify(fileRepository).existsByWorkloadInstanceName(anyString());
        verify(fileRepository, times(2)).save(any(), any(MultipartFile.class));
        verify(fileRepository, times(1)).save(any(), any(WorkloadInstancePostRequestDto.class));
        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldSuccessfullyPullLocalGitRepo() throws GitAPIException, IOException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        when(fileService.verifyDirectoryExist(Path.of(rootDirectory + LOCAL_REPO_GIT_PATH))).thenReturn(true);

        //Test method
        WorkloadInstanceResponseDto result = instanceService.create(dto, values, clusterConnectionInfo);

        //Verify
        assertThat(result.getUrl()).isEqualTo(EXPECTED_RESULT);

        verify(gitRepoRepository).pull(any());
        verify(fileRepository).existsByWorkloadInstanceName(anyString());
        verify(fileService).createDirectory(any(Path.class));
        verify(fileRepository, times(2)).save(any(), any(MultipartFile.class));
        verify(fileRepository, times(1)).save(any(), any(WorkloadInstancePostRequestDto.class));
        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldThrowExceptionWhenCloneLocalGitRepo() throws GitAPIException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        doNothing().when(fileService).deleteDirectory(any());
        doThrow(gitAPIException).when(gitRepoRepository).cloneRepository(any());

        //Test method
        assertThatThrownBy(() -> instanceService.create(dto, values, clusterConnectionInfo))
                .isInstanceOf(GitRepoConnectionException.class);
    }

    @Test
    void shouldThrowExceptionWhenUploadFilesToGitRepo() throws GitAPIException, IOException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        doThrow(gitAPIException).when(gitRepoRepository).saveToGitRepo(anyString(), anyString(), anyString());

        //Test method
        assertThatThrownBy(() -> instanceService.create(dto, values, clusterConnectionInfo))
                .isInstanceOf(GitRepoConnectionException.class);
    }

    private void prepareFile(WorkloadInstancePutRequestDto dto, Path directory) throws IOException {
        Files.createFile(Path.of(directory.toString() + "/" + dto.getWorkloadInstanceName() + ".json"));
        FileUtils.copyDirectory(Path.of("src/test/resources/jsons").toFile(), directory.toFile());
    }

    private Path createDirectory(WorkloadInstancePutRequestDto dto) {
        return fileService.createDirectory(
                Path.of(rootDirectory + LOCAL_REPO_PATH + File.separator + dto.getWorkloadInstanceName()));
    }

    private WorkloadInstancePostRequestDto setWorkloadInstancePostRequestDto() {
        WorkloadInstancePostRequestDto postRequestDto = new WorkloadInstancePostRequestDto();
        postRequestDto.setWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        postRequestDto.setNamespace(NAMESPACE);
        postRequestDto.setHelmSourceUrl(HELM_SOURCE_URL);
        return postRequestDto;
    }

    private WorkloadInstancePutRequestDto setWorkloadInstancePutRequestDto(String helmSourceUrl, Map<String, Object> additionalParameters) {
        var putRequestDto = new WorkloadInstancePutRequestDto();
        putRequestDto.setWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        Optional.ofNullable(helmSourceUrl).ifPresent(putRequestDto::setHelmSourceUrl);
        Optional.ofNullable(additionalParameters).ifPresent(putRequestDto::setAdditionalParameters);
        return putRequestDto;
    }
}
