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
package com.ericsson.oss.management.onboarding.models;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DOCKER_LAYER_CONTENT_TYPE;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Manifest {

    private final int schemaVersion = 2;
    private final String mediaType = DOCKER_LAYER_CONTENT_TYPE;
    private final DockerLayer config;
    private final List<DockerLayer> layers;

}
