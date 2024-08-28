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

package com.ericsson.oss.management.onboarding.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.presentation.services.coordinator.CoordinatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private OnboardingController onboardingController;

    @MockBean
    private CoordinatorService coordinatorService;

    private static final String ARCHIVE_NAME = "test.csar";
    private static final String ARCHIVE_CONTENT = "some archive content";

    @Test
    void shouldReturnStatusCodeAcceptedWhenOnboardingStartPerforming() {
        MultipartFile csarArchive = new MockMultipartFile(ARCHIVE_NAME, ARCHIVE_CONTENT.getBytes());
        when(coordinatorService.onboard(any())).thenReturn(new CsarOnboardingResponseDto());

        HttpStatus result = onboardingController.onboardingPost(csarArchive).getStatusCode();

        assertThat(result).isEqualTo(HttpStatus.CREATED);
    }

}
