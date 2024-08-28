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

import java.nio.file.Path;

import com.ericsson.oss.management.onboarding.models.CsarPackage;

/**
 * Service for work with CSAR's
 */
public interface CSARService {

    /**
     * Unpack CSAR to appropriate files
     *
     * @param path to CSAR
     * @param timeout
     * @return CsarPackage object which contains its components
     */
    CsarPackage unpack(Path path, int timeout);
}
