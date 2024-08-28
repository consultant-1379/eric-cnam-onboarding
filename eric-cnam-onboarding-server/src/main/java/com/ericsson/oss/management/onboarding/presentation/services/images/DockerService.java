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
package com.ericsson.oss.management.onboarding.presentation.services.images;

import java.nio.file.Path;
import java.util.List;

import com.ericsson.oss.management.onboarding.models.DockerImage;

/**
 * Service that works with docker images
 */
public interface DockerService {

    /**
     * Unpack docker.tar to list of java objects
     *
     * @param pathToTar path to the storage location of docker tar file
     * @return A list of docker images that have been unpacked
     */
    List<DockerImage> unpackDockerTar(Path pathToTar);

    /**
     * Upload image within a tar file to Docker Registry
     *
     * @param image to process
     * @param directoryWithImages path to directory with images
     */
    void uploadImage(DockerImage image, Path directoryWithImages);

}
