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

import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.models.CommandResponse;
import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.HelmSource;
import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;
import com.ericsson.oss.management.onboarding.utils.CommandBuilder;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(classes = RegistryServiceImpl.class)
class RegistryServiceImplTest {

    private static final String HELMFILE_PATH = "localhost:5000/eric-bss-bam-helmfile:3.6.0_14";
    private static final String HELMCHART_PATH = "localhost:5000/charts/eric-cm-mediator:7.16.0+29";
    private static final String ORAS_PUSH_COMMAND = "oras push command";
    private static final String HELM_PUSH_COMMAND = "helm push command";

    @Autowired
    private RegistryServiceImpl ociService;

    @MockBean
    private CommandBuilder commandBuilder;
    @MockBean
    private CommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        setField(ociService, "registryUrl", "localhost:5000");
    }

    @Test
    void shouldPerformSaveDataToRegistryWithoutAnyExceptions() {
        when(commandBuilder.buildOrasPushCommand(any(), anyString())).thenReturn(ORAS_PUSH_COMMAND);
        when(commandBuilder.buildHelmPushCommand(any(), anyString())).thenReturn(HELM_PUSH_COMMAND);
        CommandResponse successResponse = new CommandResponse("", 0);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(successResponse);

        OCIRegistryResponse result = ociService.saveDataToRegistry(createCsarPackage());

        assertThatCode(() -> ociService.saveDataToRegistry(createCsarPackage()))
                .doesNotThrowAnyException();

        assertThat(result.getHelmfileUrl()).isEqualTo(HELMFILE_PATH);
        assertThat(result.getHelmChartUrls().get(0)).isEqualTo(HELMCHART_PATH);
    }

    @Test
    void shouldPerformSaveDataToRegistryWithExceptions() {
        CommandResponse responseWithException = new CommandResponse("", 1);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(responseWithException);
        when(commandBuilder.buildOrasPushCommand(any(), anyString())).thenReturn(ORAS_PUSH_COMMAND);
        var csarPackage = createCsarPackage();

        assertThatThrownBy(() -> ociService.saveDataToRegistry(csarPackage))
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Failed to push helmfile archive to OCI registry: ");
    }

    @Test
    void shouldPerformSaveDataToRegistryWithFailedPushOfHelmChart() {
        when(commandBuilder.buildOrasPushCommand(any(), anyString())).thenReturn(ORAS_PUSH_COMMAND);
        when(commandBuilder.buildHelmPushCommand(any(), anyString())).thenReturn(HELM_PUSH_COMMAND);
        CommandResponse successResponse = new CommandResponse("", 0);
        when(commandExecutor.execute(eq(ORAS_PUSH_COMMAND), anyInt())).thenReturn(successResponse);
        CommandResponse responseWithException = new CommandResponse("", 1);
        when(commandExecutor.execute(eq(HELM_PUSH_COMMAND), anyInt())).thenReturn(responseWithException);

        var csarPackage = createCsarPackage();

        assertThatThrownBy(() -> ociService.saveDataToRegistry(csarPackage))
                .isInstanceOf(InternalRuntimeException.class)
                .hasMessageContaining("Failed to push helm chart to OCI registry: ");
    }

    @Test
    void shouldPerformSaveDataToRegistryWithNullHelmfile() {
        CommandResponse successResponse = new CommandResponse("", 0);
        when(commandExecutor.execute(anyString(), anyInt())).thenReturn(successResponse);
        when(commandBuilder.buildOrasPushCommand(any(), anyString())).thenReturn(ORAS_PUSH_COMMAND);
        when(commandBuilder.buildHelmPushCommand(any(), anyString())).thenReturn(HELM_PUSH_COMMAND);

        OCIRegistryResponse result = ociService.saveDataToRegistry(createCsarWithNullHelmSourses());

        assertThatCode(() -> ociService.saveDataToRegistry(createCsarPackage()))
                .doesNotThrowAnyException();

        assertThat(result.getHelmfileUrl()).isEmpty();
        assertThat(result.getHelmChartUrls()).isEmpty();
    }

    private CsarPackage createCsarPackage() {
        return CsarPackage.builder()
                .helmfile(getHelmfile())
                .helmCharts(List.of(getChart()))
                .build();
    }

    private CsarPackage createCsarWithNullHelmSourses() {
        return CsarPackage.builder()
                .helmfile(null)
                .helmCharts(new ArrayList<>())
                .build();
    }

    private HelmSource getHelmfile() {
        return HelmSource.builder()
                .location(Path.of("util/eric-bss-bam-helmfile-3.6.0+14.tgz"))
                .name("eric-bss-bam")
                .version("3.6.0+14")
                .build();
    }

    private HelmSource getChart() {
        return HelmSource.builder()
                .location(Path.of("/util/eric-cm-mediator-7.16.0+29.tgz"))
                .name("eric-cm-mediator")
                .version("7.16.0+29")
                .build();
    }
}