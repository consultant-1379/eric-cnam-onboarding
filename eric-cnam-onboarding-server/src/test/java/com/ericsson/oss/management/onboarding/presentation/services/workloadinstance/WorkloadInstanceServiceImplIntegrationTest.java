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

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.CONTENT_TYPE;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.CLUSTER_CONFIG_CONTENT;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.CLUSTER_CONNECTION_INFO;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.CLUSTER_CONNECTION_INFO_CONFIG;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.CLUSTER_CONNECTION_INFO_YAML;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.GLOBAL_CRD_NAMESPACE_VALUE;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.HELM_SOURCE_URL;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.NAMESPACE;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.VALUES;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.VALUES_CONTENT;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.VALUES_YAML;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.WORKLOAD_INSTANCE_JSON;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.WORKLOAD_INSTANCE_JSON_CONTENT;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.WORKLOAD_INSTANCE_NAME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ericsson.oss.management.onboarding.presentation.exceptions.NotUniqueWorkloadInstanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.presentation.repositories.FileRepository;
import com.ericsson.oss.management.onboarding.presentation.repositories.GitRepoRepository;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.FileServiceImpl;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

@ActiveProfiles("test")
@SpringBootTest(classes = { WorkloadInstanceServiceImpl.class, FileRepository.class, FileServiceImpl.class, CommandExecutor.class})
class WorkloadInstanceServiceImplIntegrationTest {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private WorkloadInstanceServiceImpl instanceService;
    @MockBean
    private GitRepoRepository gitRepoRepository;
    @Value("${directory.root}")
    private String rootDirectory;

    private static final String NOT_UNIQUE_WORKLOAD_INSTANCE_EXCEPTION = "Workload instance name workloadinstancename must be unique";

    @AfterEach
    public void cleanUpLocalGitRepoDirectory() {
        fileService.deleteDirectory(Path.of(rootDirectory + LOCAL_REPO_PATH));
    }

    @Test
    void shouldSaveWorkloadInstanceInLocalDirectory() {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        Path workloadInstanceJson = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator + WORKLOAD_INSTANCE_JSON);

        //Test method
        instanceService.create(dto, null, null);

        //Verify
        assertThat(Files.exists(workloadInstanceJson)).isTrue();
        assertThat(fileService.readDataFromFile(workloadInstanceJson)).isEqualTo(WORKLOAD_INSTANCE_JSON_CONTENT);
    }

    @Test
    void shouldSaveFewWorkloadInstancesInLocalDirectory() {
        //Init
        WorkloadInstancePostRequestDto dtoFirst = setWorkloadInstancePostRequestDto();
        Path workloadInstanceJson = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator + WORKLOAD_INSTANCE_JSON);

        //Test method
        instanceService.create(dtoFirst, null, null);

        //Verify
        assertThat(Files.exists(workloadInstanceJson)).isTrue();
        assertThat(fileService.readDataFromFile(workloadInstanceJson)).isEqualTo(WORKLOAD_INSTANCE_JSON_CONTENT);
    }

    @Test
    void shouldThrowExceptionWhenSaveNotUniqueWorkloadInstanceName() {
        //Init
        WorkloadInstancePostRequestDto dtoFirst = setWorkloadInstancePostRequestDto();
        instanceService.create(dtoFirst, null, null);
        //Test method
        assertThatThrownBy(() -> instanceService.create(dtoFirst, null, null))
                .isInstanceOf(NotUniqueWorkloadInstanceException.class)
                .hasMessageContaining(NOT_UNIQUE_WORKLOAD_INSTANCE_EXCEPTION);
    }

    @Test
    void shouldSaveWorkloadInstanceAndClusterInfoFileInLocalDirectory() throws IOException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        Path workloadInstanceJson = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator +
                                                      WORKLOAD_INSTANCE_JSON);
        Path clusterInfo = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator + CLUSTER_CONNECTION_INFO_CONFIG);
        MockMultipartFile clusterInfoFile = getClusterInfoFile();

        //Test method
        instanceService.create(dto, null, clusterInfoFile);

        //Verify
        assertThat(Files.exists(clusterInfo)).isTrue();
        assertThat(Files.exists(workloadInstanceJson)).isTrue();
        assertThat(fileService.readDataFromFile(workloadInstanceJson)).isEqualTo(WORKLOAD_INSTANCE_JSON_CONTENT);
        assertThat(fileService.readDataFromFile(clusterInfo)).isEqualTo(CLUSTER_CONFIG_CONTENT);
    }

    @Test
    void shouldSaveWorkloadInstanceValuesAndClusterInfoFileInLocalDirectory() throws IOException {
        //Init
        WorkloadInstancePostRequestDto dto = setWorkloadInstancePostRequestDto();
        Path workloadInstanceJson = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator +
                                                      WORKLOAD_INSTANCE_JSON);
        Path clusterInfo = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator + CLUSTER_CONNECTION_INFO_CONFIG);
        Path values = Paths.get(
                rootDirectory + LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME + File.separator + VALUES_YAML);
        MockMultipartFile clusterInfoFile = getClusterInfoFile();
        MockMultipartFile valuesFile = getValuesFile();
        //Test method
        instanceService.create(dto, valuesFile, clusterInfoFile);

        //Verify
        assertThat(Files.exists(values)).isTrue();
        assertThat(Files.exists(clusterInfo)).isTrue();
        assertThat(Files.exists(workloadInstanceJson)).isTrue();
        assertThat(fileService.readDataFromFile(workloadInstanceJson)).isEqualTo(WORKLOAD_INSTANCE_JSON_CONTENT);
        assertThat(fileService.readDataFromFile(clusterInfo)).isEqualTo(CLUSTER_CONFIG_CONTENT);
        assertThat(fileService.readDataFromFile(values)).isEqualTo(VALUES_CONTENT);
    }

    private WorkloadInstancePostRequestDto setWorkloadInstancePostRequestDto() {
        WorkloadInstancePostRequestDto postRequestDto = new WorkloadInstancePostRequestDto();
        postRequestDto.setWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        postRequestDto.setNamespace(NAMESPACE);
        postRequestDto.crdNamespace(GLOBAL_CRD_NAMESPACE_VALUE);
        postRequestDto.setHelmSourceUrl(HELM_SOURCE_URL);
        return postRequestDto;
    }

    private MockMultipartFile getClusterInfoFile() throws IOException {
        File file = new ClassPathResource(CLUSTER_CONNECTION_INFO_YAML).getFile();
        return new MockMultipartFile(CLUSTER_CONNECTION_INFO, CLUSTER_CONNECTION_INFO_CONFIG, CONTENT_TYPE, new FileInputStream(file));
    }

    private MockMultipartFile getValuesFile() throws IOException {
        File file = new ClassPathResource(VALUES_YAML).getFile();
        return new MockMultipartFile(VALUES, VALUES_YAML, CONTENT_TYPE, new FileInputStream(file));
    }
}
