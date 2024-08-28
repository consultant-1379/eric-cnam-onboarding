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

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.METADATA_YAML;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.management.onboarding.TestUtils;

@SpringBootTest(classes = { YAMLParseUtilsTest.class })
class YAMLParseUtilsTest {

    @Test
    void shouldReturnContentOfYamlSuccessfully() throws URISyntaxException, IOException {
        Path file = TestUtils.getResource(METADATA_YAML);
        Iterable<Object> result = YAMLParseUtils.getYamlContent(Files.newInputStream(file));

        assertThat(result).isNotNull();
    }

}