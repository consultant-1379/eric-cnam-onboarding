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

package com.ericsson.oss.management.onboarding.contracts.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.presentation.controllers.OnboardingController;
import com.ericsson.oss.management.onboarding.presentation.services.coordinator.CoordinatorService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostOnboardingPositiveBase {

    @InjectMocks
    private OnboardingController onboardingController;

    @Mock
    private CoordinatorService coordinatorService;

    @BeforeEach
    void setup() {
        when(coordinatorService.onboard(any())).thenReturn(getOnboardingResponse());

        RestAssuredMockMvc.standaloneSetup(onboardingController);
    }

    private CsarOnboardingResponseDto getOnboardingResponse() {
        return new CsarOnboardingResponseDto()
                .helmfileUrl("testLink")
                .helmChartUrls(Collections.singletonList("testLink"));
    }
}
