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

package com.ericsson.oss.management.onboarding.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.CHARTS;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.CONFIG;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.HELM_COMMAND;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.INSECURE_FLAG;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.OCI_PREFIX;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.ORAS_COMMAND;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.CHANGE_DIRECTORY;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.PUSH;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.ORAS_CONFIG;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.SEMICOLON;
import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.SPACE;

/**
 * Build commands for services
 */
@Slf4j
@Component
public class CommandBuilder {

    @Value("${registry.url}")
    private String registryUrl;

    /**
     * Build oras push command for OCI registry
     *
     * @param directory location of source
     * @param path to store
     * @return full command
     */
    public String buildOrasPushCommand(Path directory, String path) {
        log.info("Building oras push command for OCI registry");
        List<String> applyList = new ArrayList<>(
                List.of(CHANGE_DIRECTORY, directory.getParent().toAbsolutePath().toString(), SEMICOLON));

        applyList.add(ORAS_COMMAND);
        applyList.add(PUSH);
        applyList.add(registryUrl + File.separator + path);
        applyList.add(CONFIG);
        applyList.add(ORAS_CONFIG);
        applyList.add(directory.getFileName().toString());
        applyList.add(INSECURE_FLAG);

        return String.join(SPACE, applyList);
    }

    /**
     * Build helm push command for OCI registry
     *
     * @param directory location of source
     * @param chartName of chart
     * @return full command
     */
    public String buildHelmPushCommand(Path directory, String chartName) {
        log.info("Building helm push command for OCI registry");
        List<String> applyList = new ArrayList<>(
                List.of(CHANGE_DIRECTORY, directory.getParent().toAbsolutePath().toString(), SEMICOLON));

        applyList.add(HELM_COMMAND);
        applyList.add(PUSH);
        applyList.add(chartName);
        applyList.add(OCI_PREFIX + registryUrl + File.separator + CHARTS);

        return String.join(SPACE, applyList);
    }
}
