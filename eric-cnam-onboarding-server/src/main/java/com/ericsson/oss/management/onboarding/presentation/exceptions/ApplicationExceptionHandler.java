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

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ericsson.oss.management.onboarding.api.model.ProblemDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_MESSAGE = "{} Occurred, {}";

    @ExceptionHandler(InvalidInputException.class)
    public final ResponseEntity<ProblemDetails> handleInvalidInputException(final InvalidInputException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("InvalidInputException");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileException.class)
    public final ResponseEntity<ProblemDetails> handleInvalidFileException(final InvalidFileException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("InvalidFileException");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(InternalRuntimeException.class)
    public final ResponseEntity<ProblemDetails> handleInternalRuntimeException(final InternalRuntimeException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("InternalRuntimeException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotSavedDockerImageException.class)
    public ResponseEntity<ProblemDetails> handleNotSavedDockerImageException(NotSavedDockerImageException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("NotSavedDockerImageException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @Override
    public final ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ERROR_MESSAGE, ex.toString(), ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("MalformedRequest");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(NothingToCommitException.class)
    public ResponseEntity<ProblemDetails> handleNotingToCommitException(NothingToCommitException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("NothingToCommitException");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(GitRepoConnectionException.class)
    public ResponseEntity<ProblemDetails> handleGitRepoConnectionException(GitRepoConnectionException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("GitRepoConnectionException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GitExecuteOperationException.class)
    public ResponseEntity<ProblemDetails> handleGitExecuteOperationException(GitExecuteOperationException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("GitExecuteOperationException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotUniqueWorkloadInstanceException.class)
    public final ResponseEntity<ProblemDetails> handleNotUniqueWorkloadInstanceException(NotUniqueWorkloadInstanceException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("NotUniqueWorkloadInstanceException");
        problemDetails.setType(CONFLICT.getReasonPhrase());
        problemDetails.setStatus(CONFLICT.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, CONFLICT);
    }

    @ExceptionHandler(DockerServiceException.class)
    public final ResponseEntity<ProblemDetails> handleDockerServiceException(final DockerServiceException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("DockerServiceException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HelmChartRegistryUnavailableException.class)
    public final ResponseEntity<ProblemDetails> handleHelmChartRegistryUnavaliableException(final HelmChartRegistryUnavailableException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("HelmChartRegistryUnavailableException");
        problemDetails.setType(SERVICE_UNAVAILABLE.getReasonPhrase());
        problemDetails.setStatus(SERVICE_UNAVAILABLE.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(LayerException.class)
    public final ResponseEntity<ProblemDetails> handleLayerException(final LayerException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("LayerException");
        problemDetails.setType(INTERNAL_SERVER_ERROR.getReasonPhrase());
        problemDetails.setStatus(INTERNAL_SERVER_ERROR.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidURLException.class)
    public final ResponseEntity<ProblemDetails> handleInvalidURLException(final InvalidURLException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("InvalidURLException");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(ZipCompressedException.class)
    public final ResponseEntity<ProblemDetails> handleZipCompressedException(final ZipCompressedException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        var problemDetails = new ProblemDetails();
        problemDetails.setTitle("ZipCompressedException");
        problemDetails.setType(BAD_REQUEST.getReasonPhrase());
        problemDetails.setStatus(BAD_REQUEST.value());
        problemDetails.setInstance("");
        problemDetails.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }
}
