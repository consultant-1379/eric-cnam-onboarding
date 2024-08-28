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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.DockerImage;
import com.ericsson.oss.management.onboarding.models.DockerInfo;
import com.ericsson.oss.management.onboarding.models.HelmSource;
import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;
import com.ericsson.oss.management.onboarding.presentation.mappers.CsarOnboardingResponseDtoMapper;
import com.ericsson.oss.management.onboarding.presentation.services.CSARService;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.RegistryService;
import com.ericsson.oss.management.onboarding.presentation.services.images.DockerService;

@SpringBootTest(classes = { CoordinatorServiceImpl.class, CsarOnboardingResponseDtoMapper.class })
class CoordinatorServiceImplTest {

    private static final String CSAR_PACKAGE_NAME = "csar-package.csar";
    private static final String HELMFILE_URL = "helmfile url";
    private static final List<String> HELM_CHART_URLS = Collections.singletonList("helmchart url");
    private static final String CONFIG = "219ee5171f8006d1462fa76c12b9b01ab672dbc8b283f186841bf2c3ca8e3c93.json";
    private static final String LAYER = "5b4ef804af565ff504bf7693239e5f26b4782484f068226b7244117760603d0d/layer.tar";
    private static final String REPO_TAG = "localhost/dockerhub-ericsson-remote/busybox:1.32.0";
    private static final Path PATH_TO_TAR = Path.of("test/layer.tar");

    @Autowired
    private CoordinatorServiceImpl coordinatorService;

    @Autowired
    private CsarOnboardingResponseDtoMapper csarOnboardingResponseDtoMapper;

    @MockBean
    private CSARService csarService;

    @MockBean
    private FileService fileService;

    @MockBean
    private RegistryService registryService;

    @MockBean
    private DockerService dockerService;

    @Mock
    private MultipartFile csarArchive;

    @Mock
    private Path directory;

    @Mock
    private Path csarPath;


    @BeforeEach
    void setup() {
        when(fileService.createDirectory()).thenReturn(directory);
        when(fileService.storeFileIn(directory, csarArchive, CSAR_PACKAGE_NAME)).thenReturn(csarPath);
    }

    @Test
    void shouldOnboardCsarSuccessfully() {
        CsarPackage csarPackage = getCsarPackage();
        OCIRegistryResponse ociRegistryResponse = new OCIRegistryResponse(HELMFILE_URL, HELM_CHART_URLS);
        when(csarService.unpack(eq(csarPath), anyInt())).thenReturn(csarPackage);
        when(registryService.saveDataToRegistry(csarPackage)).thenReturn(ociRegistryResponse);

        CsarOnboardingResponseDto result = coordinatorService.onboard(csarArchive);

        assertThat(result).isNotNull();
        assertThat(result.getHelmChartUrls()).isEqualTo(HELM_CHART_URLS);
        assertThat(result.getHelmfileUrl()).isEqualTo(HELMFILE_URL);

        verify(dockerService).uploadImage(any(), any());
    }

    private CsarPackage getCsarPackage() {
        CsarPackage csarPackage = new CsarPackage();
        csarPackage.setDockerImages(getDockerInfo());
        csarPackage.setHelmfile(new HelmSource());
        csarPackage.setHelmCharts(Collections.singletonList(new HelmSource()));

        return csarPackage;
    }

    private DockerInfo getDockerInfo() {
        DockerInfo info = new DockerInfo();
        info.setImages(Collections.singletonList(getImage()));
        info.setPathToTar(PATH_TO_TAR);
        return info;
    }

    private DockerImage getImage() {
        DockerImage image = new DockerImage();
        image.setConfig(CONFIG);
        image.setLayers(Collections.singletonList(LAYER));
        image.setRepoTags(Collections.singletonList(REPO_TAG));
        return image;
    }

}
