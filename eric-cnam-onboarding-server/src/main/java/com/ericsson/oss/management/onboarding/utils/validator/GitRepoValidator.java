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
package com.ericsson.oss.management.onboarding.utils.validator;

import com.ericsson.oss.management.onboarding.presentation.exceptions.GitExecuteOperationException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Validate Git operations
 */
@Slf4j
@Component
@NoArgsConstructor
public class GitRepoValidator {
    /**
     * Check if there are changes to be committed before execute commit
     */
    public void verifyAddOperation(Git git) throws GitAPIException {
        var status = git.status().call();
        boolean isEmpty = (status.getAdded().isEmpty()
                && status.getChanged().isEmpty()
                && status.getRemoved().isEmpty()
                && status.getModified().isEmpty());
        if (isEmpty) {
            throw new EmptyCommitException("Git: nothing to commit, working tree clean");
        } else {
            logStatus(status);
        }
    }

    /**
     * Check status of results returned by push operation
     */
    public void verifyPushOperation(Iterable<PushResult> results) {
        Set<RemoteRefUpdate> failedRemoteUpdates = StreamSupport.stream(results.spliterator(), false)
                .map(PushResult::getRemoteUpdates)
                .flatMap(Collection::stream)
                .filter(remoteRefUpdate -> !remoteRefUpdate.getStatus().equals(RemoteRefUpdate.Status.OK))
                .filter(remoteRefUpdate -> !remoteRefUpdate.getStatus().equals(RemoteRefUpdate.Status.UP_TO_DATE))
                .collect(Collectors.toSet());
        if (!failedRemoteUpdates.isEmpty()) {
            throw new GitExecuteOperationException("Git: Failed to push: " + failedRemoteUpdates);
        }
        log.info("Git: Pushed successfully");
    }


    private void logStatus(Status status) {
        log.info("Added: {}", status.getAdded());
        log.info("Changed: {}", status.getChanged());
        log.info("IgnoredNotInIndex: {}", status.getIgnoredNotInIndex());
        log.info("Missing: {}", status.getMissing());
        log.info("Modified: {}", status.getModified());
        log.info("Removed: {}", status.getRemoved());
    }
}
