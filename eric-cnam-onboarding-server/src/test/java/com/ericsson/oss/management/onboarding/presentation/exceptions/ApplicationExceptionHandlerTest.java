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
package com.ericsson.oss.management.onboarding.presentation.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.oss.management.onboarding.api.model.ProblemDetails;

class ApplicationExceptionHandlerTest {

    private static final String INTERNAL_RUNTIME_EXCEPTION = "InternalRuntimeException";
    private static final String INVALID_FILE_EXCEPTION = "InvalidFileException";
    private static final String INVALID_INPUT_EXCEPTION = "InvalidInputException";
    private static final String NOT_SAVED_DOCKER_IMAGE_EXCEPTION = "NotSavedDockerImageException";
    private static final String GIT_REPO_CONNECTION_EXCEPTION = "GitRepoConnectionException";
    private static final String GIT_EXECUTE_OPERATION_EXCEPTION = "GitExecuteOperationException";
    private static final String NOT_UNIQUE_WORKLOAD_INSTANCE_EXCEPTIONS = "NotUniqueWorkloadInstanceException";
    private static final String DOCKER_SERVICE_EXCEPTION = "DockerServiceException";
    private static final String HELM_CHART_REGISTRY_UNAVAILABLE_EXCEPTION = "HelmChartRegistryUnavailableException";
    private static final String LAYER_EXCEPTION = "LayerException";
    private static final String INVALID_URL_EXCEPTION = "InvalidURLException";
    private static final String NOTHING_TO_COMMIT_EXCEPTION = "NothingToCommitException";
    private static final String ZIP_COMPRESSED_EXCEPTION = "ZipCompressedException";

    ApplicationExceptionHandler applicationExceptionHandler = new ApplicationExceptionHandler();
    public static final String ERROR = "This is a test";

    @Test
    void handleInvalidInputExceptionTest() {
        try {
            throw new InvalidInputException(ERROR);
        } catch (InvalidInputException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleInvalidInputException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(INVALID_INPUT_EXCEPTION);
            assertThat(result.getType()).isEqualTo(BAD_REQUEST.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(BAD_REQUEST);
        }
    }

    @Test
    void handleInternalRuntimeExceptionTest() {
        try {
            throw new InternalRuntimeException(ERROR);
        } catch (InternalRuntimeException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleInternalRuntimeException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(INTERNAL_RUNTIME_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleInvalidFileExceptionTest() {
        try {
            throw new InvalidFileException(ERROR);
        } catch (InvalidFileException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleInvalidFileException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(INVALID_FILE_EXCEPTION);
            assertThat(result.getType()).isEqualTo(BAD_REQUEST.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(BAD_REQUEST);
        }
    }

    @Test
    void handleNotSavedDockerImageExceptionTest() {
        try {
            throw new NotSavedDockerImageException(ERROR);
        } catch (NotSavedDockerImageException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleNotSavedDockerImageException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(NOT_SAVED_DOCKER_IMAGE_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleGitRepoConnectionExceptionTest() {
        try {
            throw new GitRepoConnectionException(ERROR);
        } catch (GitRepoConnectionException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleGitRepoConnectionException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(GIT_REPO_CONNECTION_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleNotingToCommitExceptionTest() {
        try {
            throw new NothingToCommitException(ERROR);
        } catch (NothingToCommitException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleNotingToCommitException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(NOTHING_TO_COMMIT_EXCEPTION);
            assertThat(result.getType()).isEqualTo(BAD_REQUEST.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(BAD_REQUEST);
        }
    }

    @Test
    void handleGitExecuteOperationExceptionTest() {
        try {
            throw new GitExecuteOperationException(ERROR);
        } catch (GitExecuteOperationException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleGitExecuteOperationException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(GIT_EXECUTE_OPERATION_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleNotUniqueWorkloadInstanceExceptionTest() {
        try {
            throw new NotUniqueWorkloadInstanceException(ERROR);
        } catch (NotUniqueWorkloadInstanceException e) {
            ResponseEntity<ProblemDetails> responseEntity =
                    applicationExceptionHandler.handleNotUniqueWorkloadInstanceException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(NOT_UNIQUE_WORKLOAD_INSTANCE_EXCEPTIONS);
            assertThat(result.getType()).isEqualTo(CONFLICT.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(CONFLICT.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(CONFLICT);
        }
    }

    @Test
    void handleDockerServiceExceptionTest() {
        try {
            throw new DockerServiceException(ERROR);
        } catch (DockerServiceException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleDockerServiceException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(DOCKER_SERVICE_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleHelmChartRegistryUnavailableExceptionTest() {
        try {
            throw new HelmChartRegistryUnavailableException(ERROR);
        } catch (HelmChartRegistryUnavailableException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleHelmChartRegistryUnavaliableException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(HELM_CHART_REGISTRY_UNAVAILABLE_EXCEPTION);
            assertThat(result.getType()).isEqualTo(SERVICE_UNAVAILABLE.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(SERVICE_UNAVAILABLE.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(SERVICE_UNAVAILABLE);
        }
    }

    @Test
    void handleLayerExceptionTest() {
        try {
            throw new LayerException(ERROR);
        } catch (LayerException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleLayerException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(LAYER_EXCEPTION);
            assertThat(result.getType()).isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void handleInvalidURLException() {
        try {
            throw new InvalidURLException(ERROR);
        } catch (InvalidURLException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleInvalidURLException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(INVALID_URL_EXCEPTION);
            assertThat(result.getType()).isEqualTo(BAD_REQUEST.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(BAD_REQUEST);
        }
    }

    @Test
    void handleZipCompressedException() {
        try {
            throw new ZipCompressedException(ERROR);
        } catch (ZipCompressedException e) {
            ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleZipCompressedException(e);
            ProblemDetails result = responseEntity.getBody();
            HttpStatus resultCode = responseEntity.getStatusCode();

            assertThat(result.getTitle()).isEqualTo(ZIP_COMPRESSED_EXCEPTION);
            assertThat(result.getType()).isEqualTo(BAD_REQUEST.getReasonPhrase());
            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getInstance()).isEmpty();
            assertThat(result.getDetail()).isEqualTo(ERROR);

            assertThat(resultCode).isEqualTo(BAD_REQUEST);
        }
    }
}