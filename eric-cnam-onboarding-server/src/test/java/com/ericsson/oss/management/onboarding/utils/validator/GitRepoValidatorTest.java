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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = GitRepoValidator.class)
class GitRepoValidatorTest {

    @Autowired
    private GitRepoValidator gitRepoValidator;
    @Mock
    private Git git;
    @Mock
    private Status status;
    @Mock
    private StatusCommand statusCommand;
    @Mock
    private PushResult pushResult;
    @Mock
    private RemoteRefUpdate remoteRefUpdate;

    private static final String COMMIT_EXCEPTION_MESSAGE = "Git: nothing to commit, working tree clean";
    private static final String PUSH_EXCEPTION_MESSAGE = "Git: Failed to push";

    @Test
    void shouldThrowExceptionWhenNothingToCommit() throws GitAPIException {
        //Setup
        when(git.status()).thenReturn(statusCommand);
        when(statusCommand.call()).thenReturn(status);
        when(status.getAdded()).thenReturn(Collections.emptySet());
        //Test
        assertThatThrownBy(() -> gitRepoValidator.verifyAddOperation(git))
                .isInstanceOf(EmptyCommitException.class)
                .hasMessage(COMMIT_EXCEPTION_MESSAGE);
    }

    @Test
    void shouldSuccessfullyVerifyAddOperation() throws GitAPIException {
        //Setup
        when(git.status()).thenReturn(statusCommand);
        when(statusCommand.call()).thenReturn(status);
        when(status.getAdded()).thenReturn(Collections.emptySet());
        when(status.getChanged()).thenReturn(Collections.singleton("changed"));
        when(status.getRemoved()).thenReturn(Collections.emptySet());
        when(status.getMissing()).thenReturn(Collections.emptySet());
        when(status.getModified()).thenReturn(Collections.emptySet());
        //Test
        gitRepoValidator.verifyAddOperation(git);

        assertThatCode(() -> gitRepoValidator.verifyAddOperation(git)).doesNotThrowAnyException();
    }

    @Test
    void verifySuccessfullyVerifyPushOperation() {
        //Setup
        Iterable<PushResult> results = Collections.singleton(pushResult);
        when(pushResult.getRemoteUpdates()).thenReturn(Collections.singleton(remoteRefUpdate));
        when(remoteRefUpdate.getStatus()).thenReturn(RemoteRefUpdate.Status.OK);
        //Test
        assertThatCode(() -> gitRepoValidator.verifyPushOperation(results)).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenFailedToValidatePushResult() {
        //Setup
        Iterable<PushResult> results = Collections.singleton(pushResult);
        when(pushResult.getRemoteUpdates()).thenReturn(Collections.singleton(remoteRefUpdate));
        when(remoteRefUpdate.getStatus()).thenReturn(RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED);
        //Test
        assertThatThrownBy(() -> gitRepoValidator.verifyPushOperation(results))
             .isInstanceOf(GitExecuteOperationException.class).hasMessageContaining(PUSH_EXCEPTION_MESSAGE);
    }
}