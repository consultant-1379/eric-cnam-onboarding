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

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DOCKER_LAYER_MEDIA_TYPE_TAR;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerLayer {

    private String mediaType = DOCKER_LAYER_MEDIA_TYPE_TAR;
    private long size;
    private String digest;

    public DockerLayer(long size, String digest) {
        this.size = size;
        this.digest = "sha256:" + digest;
    }

}
