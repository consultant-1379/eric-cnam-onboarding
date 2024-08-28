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

package com.ericsson.oss.management.onboarding.presentation.repositories;

import com.ericsson.oss.management.onboarding.presentation.exceptions.GitExecuteOperationException;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.utils.git.GitWrapper;
import com.ericsson.oss.management.onboarding.utils.validator.GitRepoValidator;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.SymbolicRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = GitRepoRepository.class)
@TestPropertySource(properties = { "gitrepo.url=/local/tmp/LocalGitRepoFailed" })
 class GitRepoRepositoryTest {

    @Autowired
    private GitRepoRepository gitRepoRepository;
    @MockBean
    private FileService fileService;
    @MockBean
    private GitRepoValidator gitRepoValidator;
    @MockBean
    private GitWrapper gitWrapper;

    @Mock
    private Git git;
    @Mock
    private CommitCommand commitCommand;
    @Mock
    private TagCommand tagCommand;
    @Mock
    private PushCommand pushCommand;
    @Mock
    private AddCommand addCommand;
    @Mock
    private ListTagCommand listTagCommand;
    @Mock
    private Path directory;
    @Mock
    private CloneCommand cloneCommand;

    private static final String PATH = "src/test/resources/gitrepo";
    private static final String MESSAGE = "commit message";
    private static final String VERSION = "successfulpost-1";

    @BeforeEach
    public void setup() throws IOException {
        when(fileService.createDirectory(anyString())).thenReturn(directory);
        when(gitWrapper.open(any())).thenReturn(git);
    }

    @Test
    void shouldErrorWhenCloneGitRepoUrlUnavailable() throws GitAPIException {
        //Setup
        when(gitWrapper.cloneRepository()).thenReturn(cloneCommand);
        when(cloneCommand.call()).thenThrow(InvalidRemoteException.class);
        when(cloneCommand.setURI(anyString())).thenReturn(cloneCommand);
        when(cloneCommand.setDirectory(any())).thenReturn(cloneCommand);
        //Test method
        assertThatThrownBy(() -> gitRepoRepository.cloneRepository(PATH))
                .isInstanceOf(GitAPIException.class);
    }

    @Test
    void shouldErrorWhenPullGitRepoWithWrongLocalPath() throws IOException {
        //Setup
        when(gitWrapper.open(any())).thenThrow(RepositoryNotFoundException.class);
        //Test method
        assertThatThrownBy(() -> gitRepoRepository.pull(PATH))
                .isInstanceOf(RepositoryNotFoundException.class);
    }

    @Test
    void shouldSuccessfullyAdd() throws GitAPIException, IOException {
        //Setup
        Git gitConnection = getGitConnection();
        when(git.add()).thenReturn(addCommand);
        when(addCommand.addFilepattern(".")).thenReturn(addCommand);
        //Test method
        gitRepoRepository.add(gitConnection);
        verify(gitWrapper).open(any());
        verify(git).add();
        verify(addCommand).addFilepattern(".");
        verify(addCommand).call();
        verify(gitRepoValidator).verifyAddOperation(any(Git.class));
    }

    @Test
    void shouldSuccessfullyCommit() throws GitAPIException, IOException {
        //Setup
        Git gitConnection = getGitConnection();
        when(git.commit()).thenReturn(commitCommand);
        when(commitCommand.setMessage(MESSAGE)).thenReturn(commitCommand);
        //Test method
        gitRepoRepository.commit(MESSAGE, gitConnection);
        verify(gitWrapper).open(any());
        verify(git).commit();
        verify(commitCommand).setMessage(MESSAGE);
        verify(commitCommand).call();
    }

    @Test
    void shouldSuccessfullyTag() throws GitAPIException, IOException {
        //Setup
        Git gitConnection = getGitConnection();
        when(git.tag()).thenReturn(tagCommand);
        when(tagCommand.setName(VERSION)).thenReturn(tagCommand);
        when(tagCommand.setForceUpdate(anyBoolean())).thenReturn(tagCommand);
        //Test method
        gitRepoRepository.tag(VERSION, gitConnection);
        verify(gitWrapper).open(any());
        verify(git).tag();
        verify(tagCommand).setName(VERSION);
        verify(tagCommand).call();
    }

    @Test
    void shouldSuccessfullyPush() throws GitAPIException, IOException {
        //Setup
        Git gitConnection = getGitConnection();
        when(git.push()).thenReturn(pushCommand);
        when(pushCommand.setPushAll()).thenReturn(pushCommand);
        when(pushCommand.setPushTags()).thenReturn(pushCommand);
        //Test method
        gitRepoRepository.push(gitConnection);
        verify(gitWrapper).open(any());
        verify(gitRepoValidator).verifyPushOperation(any());
        verify(git).push();
        verify(pushCommand).setPushAll();
        verify(pushCommand).setPushTags();
        verify(pushCommand).setCredentialsProvider(any());
        verify(pushCommand).call();
    }

    @Test
    void shouldSuccessfullySaveToGitRepo() throws GitAPIException, IOException {
        //Setup
        when(git.add()).thenReturn(addCommand);
        when(addCommand.addFilepattern(".")).thenReturn(addCommand);

        when(git.commit()).thenReturn(commitCommand);
        when(commitCommand.setMessage(MESSAGE)).thenReturn(commitCommand);

        when(git.tag()).thenReturn(tagCommand);
        when(tagCommand.setName(VERSION)).thenReturn(tagCommand);
        when(tagCommand.setForceUpdate(anyBoolean())).thenReturn(tagCommand);

        when(git.push()).thenReturn(pushCommand);
        when(pushCommand.setPushAll()).thenReturn(pushCommand);
        when(pushCommand.setPushTags()).thenReturn(pushCommand);
        //Test method
        gitRepoRepository.saveToGitRepo(PATH, MESSAGE, VERSION);

        verify(gitWrapper).open(any());
        verify(git).add();
        verify(addCommand).addFilepattern(".");
        verify(addCommand).call();
        verify(gitRepoValidator).verifyAddOperation(any(Git.class));

        verify(git).commit();
        verify(commitCommand).setMessage(MESSAGE);
        verify(commitCommand).call();

        verify(git).tag();
        verify(tagCommand).setName(VERSION);
        verify(tagCommand).call();

        verify(git).push();
        verify(pushCommand).setPushAll();
        verify(pushCommand).setPushTags();
        verify(pushCommand).setCredentialsProvider(any());
        verify(pushCommand).call();
        verify(gitRepoValidator).verifyPushOperation(any());
    }

    @Test
    void shouldGetVersionList() throws GitAPIException, IOException {
        String versionName = "test";
        List<Ref> tagList = Collections.singletonList(new SymbolicRef(versionName, null));
        List<String> versionList = Collections.singletonList(versionName);
        when(gitWrapper.open(any())).thenReturn(git);
        when(git.tagList()).thenReturn(listTagCommand);
        when(listTagCommand.call()).thenReturn(tagList);

        List<String> result = gitRepoRepository.getVersionList();

        assertThat(result).isEqualTo(versionList);
    }

    @Test
    void shouldThrowExceptionWhenTagListCommandFailed() throws GitAPIException, IOException {
        String exceptionMessage = "test message";
        when(gitWrapper.open(any())).thenReturn(git);
        when(git.tagList()).thenReturn(listTagCommand);
        when(listTagCommand.call()).thenThrow(new RefNotFoundException(exceptionMessage));

        assertThatThrownBy(() -> gitRepoRepository.getVersionList())
                .isInstanceOf(GitExecuteOperationException.class)
                .hasMessageContaining(String.format("Error while fetching the versions. "
                                                            + "Details: org.eclipse.jgit.api.errors.RefNotFoundException: %s", exceptionMessage));
    }

    private Git getGitConnection() throws IOException {
        return gitWrapper.open(any());
    }
}
