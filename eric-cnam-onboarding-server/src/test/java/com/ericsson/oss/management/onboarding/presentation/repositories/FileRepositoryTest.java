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
package com.ericsson.oss.management.onboarding.presentation.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;
import static com.ericsson.oss.management.onboarding.utils.CommonTestConstants.WORKLOAD_INSTANCE_NAME;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.presentation.services.FileService;

@ActiveProfiles("test")
@SpringBootTest(classes = { FileRepository.class })
class FileRepositoryTest {
    @Autowired
    private FileRepository fileRepository;
    @MockBean
    private FileService fileService;
    @MockBean
    private MultipartFile file;

    @Test
    void shouldReturnFalseIfDirectoryIsEmpty() {
        when(fileService.createDirectory(anyString())).thenReturn(Path.of(LOCAL_REPO_PATH));

        boolean workloadInstance = fileRepository.existsByWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        assertThat(workloadInstance).isFalse();
    }

    @Test
    void shouldReturnFalseIfDirectoryIsNotExist() {
        boolean workloadInstance = fileRepository.existsByWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        assertThat(workloadInstance).isFalse();
    }

    @Test
    void shouldReturnFalseIfDirectoryContainsTheSameWorkloadInstanceName() {
        when(fileService.createDirectory(anyString())).thenReturn(Path.of(LOCAL_REPO_PATH + File.separator + WORKLOAD_INSTANCE_NAME));

        boolean workloadInstance = fileRepository.existsByWorkloadInstanceName(WORKLOAD_INSTANCE_NAME);
        assertThat(workloadInstance).isFalse();
    }
}