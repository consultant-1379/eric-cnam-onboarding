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
package com.ericsson.oss.management.onboarding.utils.git;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
/**
 * Created to wrap org.eclipse.jgit.api.Git and be able to write unit tests where we used Git.
 */
public interface GitWrapper {
    /**
     * @param file - git file directory
     * @return instance of Git
     */
    Git open(File file) throws IOException;
    /**
     * @return instance of CloneCommand
     */
    CloneCommand cloneRepository();
}
