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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CommandBuilder.class)
@TestPropertySource(properties = { "registry.url = localhost:5000" })
class CommandBuilderTest {

    @Autowired
    private CommandBuilder commandBuilder;

    private static final String PATH = "eric-bss-bam-helmfile-3.6.0+14.tgz";
    private static final Path DIRECTORY = Path.of("util/eric-bss-bam-helmfile-3.6.0+14.tgz");
    private static final String EXPECTED_RESULT = "oras push localhost:5000/eric-bss-bam-helmfile-3.6.0+14.tgz --config" +
            " /dev/null:application/vnd.acme.rocket.config eric-bss-bam-helmfile-3.6.0+14.tgz --insecure";
    private static final String EXPECTED_HELM_PUSH_RESULT = "helm push eric-bss-bam-helmfile-3.6.0+14.tgz oci://localhost:5000/charts/";

    @Test
    void successfullyBuildPushCommand() {
        String result = commandBuilder.buildOrasPushCommand(DIRECTORY, PATH);

        assertThat(result).contains(EXPECTED_RESULT);
    }

    @Test
    void successfullyBuildHelmPushCommand() {
        String result = commandBuilder.buildHelmPushCommand(DIRECTORY, PATH);

        assertThat(result).contains(EXPECTED_HELM_PUSH_RESULT);
    }

}