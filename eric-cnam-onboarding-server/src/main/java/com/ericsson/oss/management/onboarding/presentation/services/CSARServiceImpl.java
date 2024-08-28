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

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.CHARTS_DIR;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.CHART_YAML;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.METADATA_YAML;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.VERSION_KEY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.ericsson.oss.management.onboarding.presentation.constants.FileDetails;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.DockerImage;
import com.ericsson.oss.management.onboarding.models.DockerInfo;
import com.ericsson.oss.management.onboarding.models.HelmSource;
import com.ericsson.oss.management.onboarding.presentation.services.images.DockerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CSARServiceImpl implements CSARService {

    private final FileService fileService;
    private final DockerService dockerService;

    private static final String HELMFILE_LOCATION = "Definitions/OtherTemplates";
    private static final String CHARTS_LOCATION = HELMFILE_LOCATION + "/helm-charts.tgz";
    private static final String DOCKER_IMAGES_LOCATION = "Files/images/docker.tar";

    @Override
    public CsarPackage unpack(Path path, int timeout) {
        log.info("Start to unzip CSAR package");
        fileService.unzip(path, timeout);
        log.info("CSAR package is unzipped");
        var parentDirectory = path.getParent();
        Path helmfileLocation = findHelmfile(fileService.getFileFromDirectory(parentDirectory, HELMFILE_LOCATION));
        Path charts = fileService.getFileFromDirectory(parentDirectory, CHARTS_LOCATION);
        Path dockerArchive = fileService.getFileFromDirectory(parentDirectory, DOCKER_IMAGES_LOCATION);
        log.info("Proper directories are extracted from unzipped package");
        HelmSource helmfile = Optional.ofNullable(helmfileLocation)
                .map(item -> prepareHelmSource(item, timeout))
                .orElse(null);
        List<HelmSource> integrationCharts = Optional.ofNullable(charts)
                .map(item -> getCharts(item, timeout))
                .orElse(null);
        var csarPackage = CsarPackage.builder()
                .helmfile(helmfile)
                .helmCharts(integrationCharts)
                .dockerImages(prepareDockerInfo(dockerArchive))
                .build();
        log.info("Csar package is successfully unpacked");
        return csarPackage;
    }

    private Path findHelmfile(Path otherTemplates) {
        log.info("Search for helmfile in {}", otherTemplates.getFileName().toString());
        try (Stream<Path> walk = Files.walk(otherTemplates)) {
            return walk
                    .filter(item -> item.getFileName().toString().contains("helmfile"))
                    .findAny().orElse(null);
        } catch (IOException e) {
            throw new InvalidFileException(String.format("There is the problem with finding helmfile "
                                                                 + "package in CSAR. Details: %s", e.getMessage()));
        }
    }

    private List<HelmSource> getCharts(Path charts, int timeout) {
        log.info("Starting to extract individual charts from common package");
        Path directory = fileService.createDirectory(charts.getParent().resolve("helm-charts"));
        fileService.untar(charts, directory, timeout);
        try (Stream<Path> walk = Files.walk(directory)) {
            log.info("Prepare and collect charts");
            return walk
                    .filter(item -> item.toFile().isFile())
                    .map(item -> prepareHelmSource(item, timeout))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new InvalidFileException("Failed to go through charts. Details: " + e.getMessage());
        }
    }

    private HelmSource prepareHelmSource(Path archive, int timeout) {
        log.info("Prepare helm source, can be helmfile or integration chart");
        Path directory = fileService.createDirectory(archive.getParent().resolve("helmsource"));
        fileService.untar(archive, directory, timeout);
        log.info("Find file, containing metadata such as name and version");
        Path file = Optional.ofNullable(
                fileService.getFileFromTheDirectoryByNamePartExcludingLocation(directory, METADATA_YAML, CHARTS_DIR))
                .orElse(fileService.getFileFromTheDirectoryExcludingLocation(directory, CHART_YAML, CHARTS_DIR));
        var helmSource = mapHelmSourceIfPossible(file, archive);
        fileService.deleteDirectory(directory);
        return helmSource;
    }

    private HelmSource mapHelmSourceIfPossible(Path file, Path archive) {
        if (file != null) {
            String version = fileService.getValueByPropertyFromFile(file, VERSION_KEY);
            String name = fileService.getValueByPropertyFromFile(file, FileDetails.NAME_KEY);
            log.info("Source {} has name = {} and version {}", archive.getFileName().toString(), name, version);
            return HelmSource.builder()
                    .version(version)
                    .name(name)
                    .location(archive)
                    .build();
        }
        return null;
    }

    private DockerInfo prepareDockerInfo(Path tarPath) {
        List<DockerImage> dockerImages = Optional.ofNullable(tarPath)
                        .map(dockerService::unpackDockerTar)
                        .orElse(null);
        return DockerInfo.builder()
                .images(dockerImages)
                .pathToTar(tarPath)
                .build();
    }

}
