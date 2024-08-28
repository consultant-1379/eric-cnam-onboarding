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

package com.ericsson.oss.management.onboarding.acceptance.steps;

import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.ONBOARDING_URL;
import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.SERVICE_IP;
import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.SERVICE_PORT;
import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.WORKLOAD_INSTANCES_URL;
import static com.ericsson.oss.management.onboarding.acceptance.utils.FileUtils.getFileResource;

import java.util.HashMap;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Operations {

    private static final RestTemplate REST_TEMPLATE;

    static {
        REST_TEMPLATE = new RestTemplate();
    }

    private Operations() {
    }

    @Step("Create a Workload instance")
    public static ResponseEntity<HashMap<String, Object>> createWorkloadInstance(String pathToWorkloadInstanceJson,
                                                                                 String pathToValuesFile, String pathToClusterConnectionInfo) {
        log.info("Create a Workload instance");
        var url = String.format(WORKLOAD_INSTANCES_URL, SERVICE_IP, SERVICE_PORT);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = createWorkloadInstancesHttpEntity(pathToWorkloadInstanceJson,
                                                                                                    pathToValuesFile, pathToClusterConnectionInfo);
        ResponseEntity<HashMap<String, Object>> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity,
                                                                                  new ParameterizedTypeReference<>() { });
        log.info("Response is {}", response);
        return response;
    }

    @Step("Post the CSAR")
    public static ResponseEntity<HashMap<String, Object>> postCsar(String keyName, String value) {
        log.info("Post the CSAR");
        var url = String.format(ONBOARDING_URL, SERVICE_IP, SERVICE_PORT);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = createOnboardingHttpEntity(keyName, value);

        ResponseEntity<HashMap<String, Object>> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity,
                                                                                  new ParameterizedTypeReference<>() { });
        log.info("Response is {}", response);
        return response;
    }

    private static HttpEntity<MultiValueMap<String, Object>> createOnboardingHttpEntity(String keyName, String pathToCsar) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(keyName, getFileResource(pathToCsar));

        return new HttpEntity<>(body);
    }

    private static HttpEntity<MultiValueMap<String, Object>> createWorkloadInstancesHttpEntity(String pathToWorkloadInstanceJson,
                                                                                               String pathToValuesFile,
                                                                                               String pathToClusterConnectionInfoFile) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        body.add("workloadInstancePostRequestDto", getFileResource(pathToWorkloadInstanceJson));
        if (pathToValuesFile != null) {
            body.add("values", getFileResource(pathToValuesFile));
        }
        if (pathToClusterConnectionInfoFile != null) {
            body.add("clusterConnectionInfo", getFileResource(pathToClusterConnectionInfoFile));
        }
        return new HttpEntity<>(body, headers);
    }
}