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

package com.ericsson.oss.management.onboarding.presentation.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;

@SpringBootTest(classes = CsarOnboardingResponseDtoMapper.class)
class CsarOnboardingResponseDtoMapperTest {

    private static final String HELMFILE_URL = "test_url";
    private static final List<String> HELMCHART_URLS = Collections.singletonList("test_url");

    @Autowired
    private CsarOnboardingResponseDtoMapper csarOnboardingResponseDtoMapper;

    @Test
    void shouldMapOCIRegistryResponseToCsarOnboardingResponseDto() {
        OCIRegistryResponse ociRegistryResponse = getOCIRegistryResponse();

        CsarOnboardingResponseDto result = csarOnboardingResponseDtoMapper.map(ociRegistryResponse, CsarOnboardingResponseDto.class);

        assertThat(result).isNotNull();
        assertThat(result.getHelmfileUrl()).isEqualTo(HELMFILE_URL);
        assertThat(result.getHelmChartUrls()).isEqualTo(HELMCHART_URLS);
    }

    private OCIRegistryResponse getOCIRegistryResponse() {
        return OCIRegistryResponse
                .builder()
                .helmfileUrl(HELMFILE_URL)
                .helmChartUrls(HELMCHART_URLS)
                .build();
    }
}
