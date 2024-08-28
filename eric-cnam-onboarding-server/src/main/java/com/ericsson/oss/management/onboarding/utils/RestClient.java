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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.http.NoHttpResponseException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.management.onboarding.presentation.exceptions.HelmChartRegistryUnavailableException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestClient {

    private static final String LOCATION = "Location";
    private static final String UNKNOWN_EXCEPTION_LOG_MESSAGE = "Unknown exception occurred:";
    private static final String UNKNOWN_EXCEPTION_ERROR_MESSAGE = "Unknown exception occurred: %s : %s";

    private final RestTemplate restTemplate;
    private final RetryOperations retryOperations;

    /**
     * Execute REST for HEAD with headers
     *
     * @param url
     * @param username
     * @param password
     * @return HttpStatus
     */
    public HttpStatus head(final String url, final String username, String password) {
        final var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authenticationHeader(username, password));
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            return retryOperations.execute(retryContext -> restTemplate.exchange(url, HttpMethod.HEAD, entity, String.class).getStatusCode());
        } catch (final HttpClientErrorException.NotFound clientEx) {
            return HttpStatus.NOT_FOUND;
        } catch (final Exception ex) {
            log.error(UNKNOWN_EXCEPTION_LOG_MESSAGE, ex);
            throw new InternalRuntimeException(String.format(UNKNOWN_EXCEPTION_ERROR_MESSAGE, url, ex.getMessage()));
        }
    }

    /**
     * Execute REST PUT with file as body
     *
     * @param url
     * @param body
     * @param user
     * @param password
     * @param contentType
     * @return ResponseEntity response
     */
    public ResponseEntity<String> put(String url, String body, String user, String password, String contentType) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", contentType);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, authenticationHeader(user, password));

        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
        return retryOperations.execute(retryContext -> request(url, entity, HttpMethod.PUT));
    }

    /**
     * Execute REST POST with Authentication
     *
     * @param url
     * @param user
     * @param password
     * @return ResponseEntity
     */
    public ResponseEntity<String> post(String url, String user, String password) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authenticationHeader(user, password));

        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<String> exchange = retryOperations.execute(retryContext -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
        int responseCode = exchange.getStatusCode().value();

        if (responseCode == 308) { //308 redirects are not followed by redirection strategy
            List<String> locationList = exchange.getHeaders().get(LOCATION);
            Optional<String> location = getLocation(locationList);
            if (location.isPresent()) {
                exchange = retryOperations.execute(retryContext -> restTemplate.exchange(location.get(), HttpMethod.POST, entity, String.class));
            }
        }
        return exchange;
    }

    private Optional<String> getLocation(List<String> locationList) {
        if (!CollectionUtils.isEmpty(locationList)) {
            return locationList.stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    private String authenticationHeader(String user, String password) {
        var auth = String.format("%s:%s", user, password);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
        return "Basic " + new String(encodedAuth);
    }

    private void handleException(Throwable e) {
        if (e instanceof NoHttpResponseException) {
            log.error("Helm chart registry cannot be contacted, {}", e.getMessage());
            throw new HelmChartRegistryUnavailableException(e.getMessage());
        }
    }

    private ResponseEntity<String> request(String url, HttpEntity<Object> entity, HttpMethod httpMethod) {
        String message;
        HttpStatus statusCode;
        try {
            return restTemplate.exchange(url, httpMethod, entity, String.class);
        } catch (HttpClientErrorException e) {
            statusCode = e.getStatusCode();
            message = String.format("Failed to upload body to %s with Http response %s ", url, statusCode);
        } catch (Exception e) {
            log.warn(e.getMessage());
            message = e.getMessage();
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            handleException(e);
        }
        try {
            JSONObject response = new JSONObject()
                    .put("success", false)
                    .put("message", message);
            return new ResponseEntity<>(response.toString().replace("\\", ""), statusCode);
        } catch (JSONException e) {
            return new ResponseEntity<>(String.format("Failed to generate json response%n%s",
                                                      message.replace("\\", "")), statusCode);
        }
    }

}
