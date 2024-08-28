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

import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.SLASH;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.CONFIG_EXT;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.JSON_EXT;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_GIT_PATH;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.YAML_EXT;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePutRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;
import com.ericsson.oss.management.onboarding.models.WorkloadInstance;
import com.ericsson.oss.management.onboarding.presentation.exceptions.GitRepoConnectionException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.NotUniqueWorkloadInstanceException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.NothingToCommitException;
import com.ericsson.oss.management.onboarding.presentation.repositories.FileRepository;
import com.ericsson.oss.management.onboarding.presentation.repositories.GitRepoRepository;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.utils.git.GitVersioningUtils;
import com.ericsson.oss.management.onboarding.utils.validator.WorkloadInstanceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadInstanceServiceImpl implements WorkloadInstanceService {

    private static final String EXCEPTION_MESSAGE = "Can't write data. Details: %s";

    @Value("${directory.root}")
    private String rootDirectory;
    @Value("${gitrepo.url}")
    private String url;

    private final GitRepoRepository gitRepoRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @Override
    public WorkloadInstanceResponseDto create(WorkloadInstancePostRequestDto requestDto,
                                              MultipartFile values, MultipartFile clusterConnectionInfo) {
        log.info("Request is {}", requestDto);
        prepareLocalRepository();
        saveAllPartsOfRequestToTheDirectory(values, clusterConnectionInfo, requestDto);
        uploadFilesToGitRepo(requestDto.getWorkloadInstanceName());

        return buildRepoUrl(requestDto.getWorkloadInstanceName());
    }

    @Override
    public WorkloadInstanceResponseDto update(WorkloadInstancePutRequestDto requestDto, MultipartFile values) {
        log.info("Request is {}", requestDto);
        prepareLocalRepository();
        updateAllPartsOfRequestInTheDirectory(values, requestDto);
        uploadFilesToGitRepo(requestDto.getWorkloadInstanceName());

        return buildRepoUrl(requestDto.getWorkloadInstanceName());
    }

    private void saveAllPartsOfRequestToTheDirectory(MultipartFile values, MultipartFile clusterConnectionInfo,
                                                     WorkloadInstancePostRequestDto requestDto) {
        validationPostRequestDto(requestDto);

        Path directory = fileService.createDirectory(
                Path.of(rootDirectory + LOCAL_REPO_PATH + File.separator + requestDto.getWorkloadInstanceName()));
        var workloadInstancePath = getWorkloadInstanceJsonPath(directory, requestDto.getWorkloadInstanceName());
        var valuePath = getValueYamlPath(directory, values);
        var clusterConnectionInfoPath = getClusterInfoConfigPath(directory, clusterConnectionInfo);

        fileRepository.save(workloadInstancePath, requestDto);
        fileRepository.save(valuePath, values);
        fileRepository.save(clusterConnectionInfoPath, clusterConnectionInfo);
        log.info("Workload instance saved in the directory {}", directory);
    }

    private void updateAllPartsOfRequestInTheDirectory(MultipartFile values, WorkloadInstancePutRequestDto requestDto) {
        var directory = getDirectory(requestDto);

        var workloadInstancePath = getWorkloadInstanceJsonPath(directory, requestDto.getWorkloadInstanceName());
        var valuePath = getValueYamlPath(directory, values);

        setParametersIfRequired(workloadInstancePath, requestDto);
        setValuesIfRequired(valuePath, values);
        log.info("Workload instance updated in the directory {}", directory);
    }

    public void setParametersIfRequired(Path workloadInstancePath, WorkloadInstancePutRequestDto requestDto) {
        var workloadInstanceLightweight = convertFileToInstance(workloadInstancePath);

        updateHelmSource(requestDto, workloadInstanceLightweight);
        updateAdditionalParameters(requestDto, workloadInstanceLightweight);
        writeInstanceToFile(workloadInstancePath, workloadInstanceLightweight);
    }

    private WorkloadInstance convertFileToInstance(Path workloadInstancePath) {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(workloadInstancePath.toFile(), WorkloadInstance.class);
        } catch (IOException e) {
            throw new InvalidFileException(String.format(EXCEPTION_MESSAGE, e.getMessage()));
        }
    }

    private void writeInstanceToFile(Path workloadInstancePath, WorkloadInstance workloadInstanceLightweight) {
        var objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(workloadInstancePath.toFile(), workloadInstanceLightweight);
        } catch (IOException e) {
            throw new InvalidFileException(String.format(EXCEPTION_MESSAGE, e.getMessage()));
        }
    }

    private void updateHelmSource(WorkloadInstancePutRequestDto requestDto, WorkloadInstance workloadInstanceLightweight) {
        Optional.ofNullable(requestDto)
                .filter(this::isNotEmpty)
                .map(WorkloadInstancePutRequestDto::getHelmSourceUrl)
                .ifPresent(workloadInstanceLightweight::setHelmSourceUrl);
    }

    private void updateAdditionalParameters(WorkloadInstancePutRequestDto requestDto, WorkloadInstance workloadInstanceLightweight) {
        Optional.ofNullable(requestDto.getAdditionalParameters())
                .filter(this::isNotEmpty)
                .ifPresent(workloadInstanceLightweight::setAdditionalParameters);
    }

    private void setValuesIfRequired(Path valuePath, MultipartFile values) {
        fileRepository.save(valuePath, values);
    }

    private boolean isNotEmpty(WorkloadInstancePutRequestDto requestDto) {
        return StringUtils.isNotEmpty(requestDto.getHelmSourceUrl());
    }

    private boolean isNotEmpty(Map<?, ?> map) {
        return !(map == null || map.isEmpty());
    }

    private void validationPostRequestDto(WorkloadInstancePostRequestDto requestDto) {
        WorkloadInstanceValidator.validate(requestDto);
        boolean isWorkloadWithSameNameExist = fileRepository.existsByWorkloadInstanceName(requestDto.getWorkloadInstanceName());
        if (isWorkloadWithSameNameExist) {
            throw new NotUniqueWorkloadInstanceException(String.format("Workload instance name %s must be unique",
                                                                       requestDto.getWorkloadInstanceName()));
        }
    }

    private void prepareLocalRepository() {
        try {
            if (fileService.verifyDirectoryExist(Paths.get(rootDirectory + LOCAL_REPO_GIT_PATH))) {
                gitRepoRepository.pull(rootDirectory + LOCAL_REPO_PATH);
            } else {
                gitRepoRepository.cloneRepository(rootDirectory + LOCAL_REPO_PATH);
            }
        } catch (GitAPIException | IOException ex) {
            fileService.deleteDirectory(Paths.get(rootDirectory + LOCAL_REPO_PATH));
            throw new GitRepoConnectionException(String.format("Error when try to clone or pull git repository. Details: %s", ex));
        }
    }

    private void uploadFilesToGitRepo(String workloadInstanceName) {
        var versionList = gitRepoRepository.getVersionList();
        var workloadInstanceVersion = GitVersioningUtils
                .createNextVersionForWorkloadInstance(versionList, workloadInstanceName);
        var commitMessage = String.format("Create version %s of workload instance with name %s", workloadInstanceVersion, workloadInstanceName);
        try {
            gitRepoRepository.saveToGitRepo(rootDirectory + LOCAL_REPO_PATH, commitMessage, workloadInstanceVersion);
            log.info("Files are uploaded to git repository");
        } catch (EmptyCommitException ex) {
            throw new NothingToCommitException(String.format("Error when trying to update. If you want to make update" +
                    " you need to add some changes in the request. Details: %s", ex));
        } catch (GitAPIException | IOException ex) {
            throw new GitRepoConnectionException(String.format("Error when try to upload. Details: %s", ex));
        }
    }

    private WorkloadInstanceResponseDto buildRepoUrl(String workloadInstanceName) {
        var responseDto = new WorkloadInstanceResponseDto();
        responseDto.setUrl(url + SLASH + workloadInstanceName);
        return responseDto;
    }

    private Path getClusterInfoConfigPath(Path directory, MultipartFile clusterConnectionInfo) {
        return clusterConnectionInfo == null ? directory :
                Paths.get(directory.toString() + File.separator + clusterConnectionInfo.getName() + CONFIG_EXT);
    }

    private Path getValueYamlPath(Path directory, MultipartFile value) {
        return value == null ? directory :
                Paths.get(directory.toString() + File.separator + value.getName() + YAML_EXT);
    }

    private Path getWorkloadInstanceJsonPath(Path directory, String workloadInstanceName) {
        return Paths.get(directory.toString() + File.separator + workloadInstanceName + JSON_EXT);
    }

    private Path getDirectory(WorkloadInstancePutRequestDto requestDto) {
        return Path.of(rootDirectory + LOCAL_REPO_PATH + File.separator + requestDto.getWorkloadInstanceName());
    }
}
