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
package com.ericsson.oss.management.onboarding.presentation.services.layers;

import java.nio.file.Path;

import com.ericsson.oss.management.onboarding.presentation.exceptions.LayerException;

public interface LayerService {

    /**
     * Push layer to Docker Registry
     *
     * @param layerPath
     * @param uploadUrl
     * @param layerDigest
     * @throws LayerException
     */
    void pushLayer(Path layerPath, String uploadUrl, String layerDigest) throws LayerException;

}
