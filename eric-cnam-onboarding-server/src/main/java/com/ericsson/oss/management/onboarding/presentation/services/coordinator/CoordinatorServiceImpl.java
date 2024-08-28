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

package com.ericsson.oss.management.onboarding.presentation.services.coordinator;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.presentation.mappers.CsarOnboardingResponseDtoMapper;
import com.ericsson.oss.management.onboarding.presentation.services.CSARService;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.RegistryService;
import com.ericsson.oss.management.onboarding.presentation.services.images.DockerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinatorServiceImpl implements CoordinatorService {

    private static final String CSAR_PACKAGE = "csar-package.csar";
    private final CSARService csarService;
    private final FileService fileService;
    private final RegistryService registryService;
    private final CsarOnboardingResponseDtoMapper csarOnboardingResponseDtoMapper;
    private final DockerService dockerService;

    @Value("${operation.timeout}")
    private int timeout;

    @Override
    public CsarOnboardingResponseDto onboard(MultipartFile csarArchive) {
        log.info("Starting to unpack the CSAR");
        Path directory = fileService.createDirectory();
        var csarPath = fileService.storeFileIn(directory, csarArchive, CSAR_PACKAGE);
        var csarPackage = csarService.unpack(csarPath, timeout);

        log.info("Uploading data to registry");
        var ociRegistryResponse = registryService.saveDataToRegistry(csarPackage);
        Optional.ofNullable(csarPackage.getDockerImages().getPathToTar())
                .ifPresent(pathToTar -> csarPackage.getDockerImages()
                        .getImages()
                        .forEach(image -> dockerService.uploadImage(image, pathToTar.getParent())));

        fileService.deleteDirectory(directory);
        return csarOnboardingResponseDtoMapper.map(ociRegistryResponse, CsarOnboardingResponseDto.class);
    }

}
