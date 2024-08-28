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

import com.ericsson.oss.management.onboarding.api.model.CsarOnboardingResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * This service will be the main coordinating service of a request to apply CSAR archives.
 */
public interface CoordinatorService {

    /**
     *Take CSAR archive of a Post request, unpack and persist the information to registry.
     * @param csarArchive - archive to be onboarded
     * @return prepared response
     */
    CsarOnboardingResponseDto onboard(MultipartFile csarArchive);

}
