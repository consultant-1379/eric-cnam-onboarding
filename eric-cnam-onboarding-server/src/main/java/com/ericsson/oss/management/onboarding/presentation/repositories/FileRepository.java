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

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

/**
 * Upload files to local File Repository
 */
@Repository
@RequiredArgsConstructor
public class FileRepository {

    private static final String EXCEPTION_MESSAGE = "Can't write data. Details: %s";

    @Value("${directory.root}")
    private String rootDirectory;

    private final FileService fileService;

    /**
     * Check if workload instance name already exist
     *
     * @param workloadInstanceName workload instance name
     * @return boolean result of checking
     */
    public boolean existsByWorkloadInstanceName(String workloadInstanceName) {
        var localRepoPath = Paths.get(rootDirectory + LOCAL_REPO_PATH);
        if (Files.notExists(localRepoPath) || isEmpty(localRepoPath)) {
            return false;
        } else {
            return Arrays.stream(Objects.requireNonNull(new File(rootDirectory + LOCAL_REPO_PATH)
                            .listFiles(File::isDirectory)))
                    .anyMatch(file -> file.getName().equals(workloadInstanceName));
        }
    }

    /**
     * Check if workload instance name already exist
     *
     * @param directory path to save workload Instance in json format
     * @param requestDto workload instance post request dto
     */
    public void save(Path directory, WorkloadInstancePostRequestDto requestDto) {
        byte[] workloadInstanceByteArray = convertWorkloadInstanceToByteArray(requestDto);
        try {
            Files.write(directory, workloadInstanceByteArray);
        } catch (IOException e) {
            throw new InvalidFileException(String.format(EXCEPTION_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Check if workload instance name already exist
     *
     * @param directory path to save file
     * @param file file which will be saved
     */
    public void save(Path directory, MultipartFile file) {
        try {
            if (isNotEmpty(file)) {
                byte[] fileByteArray = fileService.readAsBytes(file);
                Files.write(directory, fileByteArray);
            }
        } catch (IOException e) {
            throw new InvalidFileException(String.format(EXCEPTION_MESSAGE, e.getMessage()));
        }
    }

    private byte[] convertWorkloadInstanceToByteArray(WorkloadInstancePostRequestDto requestDto) {
        var objectMapper = new ObjectMapper();
        byte[] workloadInstanceByteArray;
        try {
            workloadInstanceByteArray = objectMapper.writeValueAsBytes(requestDto);
        } catch (JsonProcessingException e) {
            throw new InvalidFileException(String.format("Can't write workloadInstance %s. Details: %s",
                    requestDto.getWorkloadInstanceName(), e.getMessage()));
        }
        return workloadInstanceByteArray;
    }

    private boolean isEmpty(Path path) {
        return Objects.requireNonNull(path.toFile().listFiles()).length == 0;
    }

    private boolean isNotEmpty(MultipartFile file)  {
        return Optional.ofNullable(file)
                .filter(this::checkFile)
                .isPresent();
    }

    private boolean checkFile(MultipartFile multipartFile) {
        try {
            return StringUtils.isNotEmpty(new String(multipartFile.getBytes()));
        } catch (IOException e) {
            throw new InvalidFileException(String.format(EXCEPTION_MESSAGE, e.getMessage()));
        }
    }
}

