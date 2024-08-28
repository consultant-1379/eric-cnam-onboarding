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

import com.ericsson.oss.management.onboarding.models.CsarPackage;
import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;

/**
 * Service for work with OCI registry
 */
public interface RegistryService {

    /**
     * Push data to OCI registry
     *
     * @param csarPackage object which contains its components
     * @return OCIRegistryResponse entity
     */
    OCIRegistryResponse saveDataToRegistry(CsarPackage csarPackage);

}


