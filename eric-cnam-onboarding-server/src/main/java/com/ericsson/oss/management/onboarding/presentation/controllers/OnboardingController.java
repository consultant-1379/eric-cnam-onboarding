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

import com.ericsson.oss.management.onboarding.api.OnboardingApi;
import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import com.ericsson.oss.management.onboarding.presentation.services.coordinator.CoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/cnonb/v1")
@RequiredArgsConstructor
public class OnboardingController implements OnboardingApi {

    private final CoordinatorService coordinatorService;

    @Override
    public ResponseEntity<CsarOnboardingResponseDto> onboardingPost(MultipartFile csarArchive) {
        log.info("Received a POST request to onboard CSAR archive");
        CsarOnboardingResponseDto response = coordinatorService.onboard(csarArchive);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
