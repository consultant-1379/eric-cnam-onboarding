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
package com.ericsson.oss.management.onboarding.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DOCKER_LAYER_CONTENT_TYPE;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryOperations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;

@SpringBootTest(classes = { RestClient.class })
class RestClientTest {

    private static final String URL = "https://localhost/test";
    private static final String LOCATION_URL = "https://localhost/location";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @Autowired
    private RestClient restClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private RetryOperations retryOperations;

    @Test
    void shouldHeadSuccessfully() {
        when(retryOperations.execute(any())).thenReturn(HttpStatus.OK);
        HttpStatus result = restClient.head(URL, USERNAME, PASSWORD);

        assertThat(result)
                .isNotNull()
                .isEqualTo(HttpStatus.OK);

        verify(retryOperations).execute(any());
    }

    @Test
    void shouldReturnNotFoundForHead() {
        when(retryOperations.execute(any())).thenThrow(HttpClientErrorException.NotFound.class);
        HttpStatus result = restClient.head(URL, USERNAME, PASSWORD);

        assertThat(result)
                .isNotNull()
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(retryOperations).execute(any());
    }

    @Test
    void shouldFailWhenUnexpectedResponse() {
        when(retryOperations.execute(any())).thenThrow(HttpClientErrorException.Forbidden.class);
        assertThatThrownBy(() -> restClient.head(URL, USERNAME, PASSWORD))
                .isInstanceOf(InternalRuntimeException.class);

        verify(retryOperations).execute(any());
    }

    @Test
    void shouldPutSuccessfully() {
        ResponseEntity<String> response = ResponseEntity.created(URI.create(URL)).build();
        when(retryOperations.execute(any())).thenReturn(response);

        ResponseEntity<String> result = restClient.put(URL, "some body", URL, PASSWORD, DOCKER_LAYER_CONTENT_TYPE);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldPostSuccessfully() {
        ResponseEntity<String> response = ResponseEntity.accepted().build();
        when(retryOperations.execute(any())).thenReturn(response);

        ResponseEntity<String> result = restClient.post(URL, USERNAME, PASSWORD);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void shouldPostAndGetLocationSuccessfully() {
        ResponseEntity<String> response = ResponseEntity
                .status(HttpStatus.PERMANENT_REDIRECT)
                .location(URI.create(LOCATION_URL)).build();
        when(retryOperations.execute(any())).thenReturn(response);

        ResponseEntity<String> result = restClient.post(URL, USERNAME, PASSWORD);

        assertThat(result).isNotNull();
    }

}
