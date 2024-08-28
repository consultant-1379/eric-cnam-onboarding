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

import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.CHARTS;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.COLON;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.SLASH;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.TIMEOUT;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.HELMFILE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ericsson.oss.management.onboarding.models.CommandResponse;
import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.HelmSource;
import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.utils.CommandBuilder;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryServiceImpl implements RegistryService {

    private final CommandBuilder commandBuilder;
    private final CommandExecutor commandExecutor;

    @Value("${registry.url}")
    private String registryUrl;

    @Override
    public OCIRegistryResponse saveDataToRegistry(CsarPackage csarPackage) {
        var helmfilePath = Optional.ofNullable(csarPackage.getHelmfile())
                .map(this::pushHelmfileAndReturnPath).orElse("");

        List<String> helmChartPaths = csarPackage.getHelmCharts().stream()
                .filter(Objects::nonNull)
                .map(this::pushHelmChartAndReturnPath)
                .collect(Collectors.toList());

        return OCIRegistryResponse.builder()
                .helmfileUrl(helmfilePath)
                .helmChartUrls(helmChartPaths)
                .build();
    }

    private String pushHelmfileAndReturnPath(HelmSource helmSource) {
        log.info("Start to push helmfile {}", helmSource.getName());
        String locationInRegistry = buildPath(helmSource);
        String command = commandBuilder.buildOrasPushCommand(getCanonicalPath(helmSource.getLocation()), locationInRegistry);
        CommandResponse response = commandExecutor.execute(command, TIMEOUT);
        if (response.getExitCode() != 0) {
            log.error("Failed to push helmfile to OCI registry. See for details in response: {}", response.getOutput());
            throw new InternalRuntimeException(String.format("Failed to push helmfile archive to OCI registry: %s", response.getOutput()));
        }
        log.info("Helmfile is successfully pushed");
        return registryUrl + SLASH + locationInRegistry;
    }

    private String pushHelmChartAndReturnPath(HelmSource chart) {
        log.info("Start to push chart {}", chart.getName());
        String chartName = chart.getLocation().getFileName().toString();
        String command = commandBuilder.buildHelmPushCommand(chart.getLocation(), chartName);
        CommandResponse response = commandExecutor.execute(command, TIMEOUT);
        if (response.getExitCode() != 0) {
            log.error("Failed to push helm chart to OCI registry. See for details in response: {}", response.getOutput());
            throw new InternalRuntimeException(String.format("Failed to push helm chart to OCI registry: %s", response.getOutput()));
        }
        log.info("Chart is successfully pushed");
        return registryUrl + SLASH + CHARTS + chart.getName() + COLON + chart.getVersion();
    }

    private String buildPath(HelmSource helmSource) {
        return helmSource.getName() +
                HELMFILE +
                COLON +
                modifyVersion(helmSource.getVersion());
    }

    private String modifyVersion(String version) {
        return version.replace("+", "_");
    }

    private Path getCanonicalPath(Path path) {
        try {
            return Path.of(path.toFile().getCanonicalPath());
        } catch (IOException ex) {
            log.error("Invalid file path: {}. Details: {}", path.toFile(), ex);
            throw new IllegalArgumentException(String.format("Invalid file path: %s", path.toFile()));
        }
    }
}
