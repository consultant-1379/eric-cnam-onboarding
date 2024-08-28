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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.DockerImage;
import com.ericsson.oss.management.onboarding.models.HelmSource;
import com.ericsson.oss.management.onboarding.presentation.services.images.DockerService;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

@SpringBootTest(classes = { CSARServiceImpl.class, FileServiceImpl.class, CommandExecutor.class })
class CSARServiceImplTest {

    @Autowired
    private CSARService csarService;

    @MockBean
    private DockerService dockerService;

    @TempDir
    private Path tempDir;

    private static final Path CSAR_PATH = Path.of("src/test/resources/csar-archives/eric-bss-bam-helmfile-3.6.0+14.csar");
    private static final int TIMEOUT = 1;

    @Test
    void shouldUnpackCsarSuccessfully() throws IOException {
        Path csar = Files.copy(CSAR_PATH, tempDir.resolve(CSAR_PATH.getFileName()));

        when(dockerService.unpackDockerTar(any())).thenReturn(getImages());

        CsarPackage result = csarService.unpack(csar, TIMEOUT);

        assertThat(result).isNotNull();
        HelmSource helmfile = result.getHelmfile();
        assertThat(helmfile).isNotNull();
        verifyFilename(helmfile.getLocation(), "eric-bss-bam-helmfile-3.6.0+14.tgz");
        assertThat(helmfile.getName()).isEqualTo("eric-bss-bam");
        assertThat(helmfile.getVersion()).isEqualTo("3.6.0+14");

        List<HelmSource> helmCharts = result.getHelmCharts();
        assertThat(helmCharts).isNotNull();
        assertThat(helmCharts.size()).isEqualTo(2);
        HelmSource chart = helmCharts.get(0);
        assertThat(chart).isNotNull();
        assertThat(chart.getName()).isEqualTo("eric-ctrl-bro");
        assertThat(chart.getVersion()).isEqualTo("6.0.0+37");

        verifyFilename(result.getDockerImages().getPathToTar(), "docker.tar");
    }

    private void verifyFilename(Path file, String name) {
        assertThat(file).isNotNull();
        assertThat(name).isEqualTo(file.getFileName().toString());
    }

    private List<DockerImage> getImages() {
        DockerImage image = new DockerImage();
        image.setConfig("config");
        image.setLayers(Collections.singletonList("layer"));
        image.setRepoTags(Collections.singletonList("tag"));
        return Collections.singletonList(image);
    }
}