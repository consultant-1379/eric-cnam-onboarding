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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.management.onboarding.presentation.exceptions.GitExecuteOperationException;
import com.ericsson.oss.management.onboarding.utils.git.GitWrapper;
import com.ericsson.oss.management.onboarding.utils.validator.GitRepoValidator;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.management.onboarding.presentation.services.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.LOCAL_REPO_PATH;

/**
 * Upload files to Git Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GitRepoRepository {

    private final FileService fileService;
    private final GitRepoValidator gitRepoValidator;
    private final GitWrapper gitWrapper;

    @Value("${directory.root}")
    private String rootDirectory;
    @Value("${gitrepo.url}")
    private String url;
    @Value("${gitrepo.username}")
    private String username;
    @Value("${gitrepo.password}")
    private String password;

    /**
     * Clone remote git repository to local folder
     *
     * @param localRepoPath path to local git repository
     */
    public void cloneRepository(String localRepoPath) throws GitAPIException {
        log.info("Start cloning git repo");
        Path directory = fileService.createDirectory(localRepoPath);

        CloneCommand command = gitWrapper.cloneRepository()
                .setURI(url)
                .setDirectory(directory.toFile());
        addCredentialsIfNeeded(command);
        try (var git = command.call()) {
            log.info("Git repo cloned successfully");
        }
    }

    /**
     * Pull from remote git repository
     *
     * @param localRepoPath path to local git repository
     */
    public void pull(String localRepoPath) throws IOException, GitAPIException {
        log.info("Start pulling a git repo");
        try (var git = openGit(localRepoPath)) {
            PullCommand command = git.pull();
            addCredentialsIfNeeded(command);
            PullResult result = command.call();

            if (!result.isSuccessful()) {
//            TODO handle merge conflict if needed, will be decided later
            } else {
                log.info("Git repo pulled successfully");
            }
        }
    }

    /**
     * Make git add
     *
     * @param git connection to git repository
     */
    public void add(Git git) throws GitAPIException {
        log.info("Start add to git repo");
        git.add().addFilepattern(".").call();
        gitRepoValidator.verifyAddOperation(git);
    }

    /**
     * Make git commit with custom message
     *
     * @param commitMessage commit message
     * @param git connection to git repository
     */
    public void commit(String commitMessage, Git git) throws GitAPIException {
        log.info("Start commit to git repo");
        git.commit().setMessage(commitMessage).call();
    }

    /**
     * Make git tag
     *
     * @param versionName version of workloadInstance
     * @param git connection to git repository
     */
    public void tag(String versionName, Git git) throws GitAPIException {
        log.info("Start tag to git repo");
        git.tag().setName(versionName).setForceUpdate(true).call();
    }

    /**
     * Make git push
     *
     * @param git connection to git repository
     */
    public void push(Git git) throws GitAPIException {
        log.info("Start push to git repo");
        PushCommand command = git.push().setPushAll().setPushTags();
        addCredentialsIfNeeded(command);
        Iterable<PushResult> results = command.call();
        gitRepoValidator.verifyPushOperation(results);
    }

    /**
     * Make save to git repo
     *
     * @param commitMessage commit message
     * @param versionName version of workloadInstance
     */
    public void saveToGitRepo(String localRepoPath, String commitMessage, String versionName) throws IOException, GitAPIException {
        log.info("Start save to git repo");
        try (var git = openGit(localRepoPath)) {
            add(git);
            commit(commitMessage, git);
            tag(versionName, git);
            push(git);
        }
    }

    /**
     * Get list of all git tag names (versions) of git repository
     * @return list of versions
     */
    public List<String> getVersionList() {
        log.info("Start get list of versions");
        try (var git = openGit(rootDirectory + LOCAL_REPO_PATH)) {
            return git.tagList()
                    .call()
                    .stream()
                    .map(Ref::getName)
                    .collect(Collectors.toList());
        } catch (IOException | GitAPIException ex) {
            throw new GitExecuteOperationException(String.format("Error while fetching the versions. Details: %s", ex));
        }
    }

    private void addCredentialsIfNeeded(TransportCommand<? extends GitCommand, ?> command) {
        if (username != null && password != null) {
            var provider = new UsernamePasswordCredentialsProvider(username, password);
            command.setCredentialsProvider(provider);
        }
    }

    private Git openGit(String path) throws IOException {
        var gitWorkDir = new File(path);
        return gitWrapper.open(gitWorkDir);
    }
}
