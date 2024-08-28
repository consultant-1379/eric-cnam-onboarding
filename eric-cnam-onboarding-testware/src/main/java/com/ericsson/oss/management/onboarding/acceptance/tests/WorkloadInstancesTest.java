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

package com.ericsson.oss.management.onboarding.acceptance.tests;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.oss.management.onboarding.acceptance.steps.Operations.createWorkloadInstance;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import com.ericsson.oss.management.onboarding.acceptance.steps.Setup;
import com.ericsson.oss.management.onboarding.acceptance.utils.RandomGenerator;

import io.qameta.allure.Description;

public class WorkloadInstancesTest {

    private static final int BASH_EXECUTION_TIMEOUT = 1;
    private static final String WORKLOAD_INSTANCE_DEFAULT_NAME = "default-name";
    private static final String WORKLOAD_INSTANCE_JSON_PATH = "src/main/resources/testData/workloadInstancePostFullOk.json";
    private static final String WORKLOAD_INSTANCE_WITHOUT_ANY_EXTRA_PARAMS_JSON_PATH = "src/main/resources/testData/workloadInstance.json";
    private static final String VALUES_PATH = "src/main/resources/testData/values.yaml";
    private static final String CLUSTER_CONNECTION_INFO_PATH = "src/main/resources/testData/cluster-connection-info.config";
    private static final String DEFAULT_GIT_REPO_URL = "http://localhost:8086/local-git-repo.git/";
    private static final String EXPECTED_VALUES_FILE_NAME = "values.yaml";
    private static final String EXPECTED_CLUSTER_CONNECTION_INFO_FILE_NAME = "clusterConnectionInfo.config";
    private static final String LOCAL_REPO_PATH = "/tmp/LocalRepository";
    private static String workloadInstanceName;


    @BeforeMethod
    public static void setUp() {
        setRandomWorkloadInstanceName();
    }

    @AfterTest
    public static void tearDown() {
        prepareWorkloadInstanceJson(WORKLOAD_INSTANCE_DEFAULT_NAME);
    }

    @Test(dataProvider = "workloadInstanceRequestData")
    @Ignore
    @Description("Post workload instance dto with and without additional params.")
    public void positiveWorkloadInstancesPost(String valuesPath, String clusterConnectionInfoPath) {
        prepareWorkloadInstanceJson(workloadInstanceName);
        ResponseEntity<HashMap<String, Object>> responseEntity = createWorkloadInstance(WORKLOAD_INSTANCE_JSON_PATH,
                                                                                        valuesPath,
                                                                                        clusterConnectionInfoPath);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasSize(1)
                .containsEntry("url", DEFAULT_GIT_REPO_URL + workloadInstanceName);

        String localGitRepoContent = Setup.execute(String.format("ls %s", LOCAL_REPO_PATH), BASH_EXECUTION_TIMEOUT);
        assertThat(localGitRepoContent).contains(workloadInstanceName);
        String workloadInstanceRepoContent = Setup.execute(String.format("ls %s/%s", LOCAL_REPO_PATH, workloadInstanceName), 1);
        assertThat(workloadInstanceRepoContent).contains(workloadInstanceName);
        if (valuesPath != null) { //if we didn't pass valuesPath in dataProvider, we won't expect creation of this file on remote
            assertThat(workloadInstanceRepoContent).contains(EXPECTED_VALUES_FILE_NAME);
        }
        if (clusterConnectionInfoPath != null) { //don't validate if didn't pass
            assertThat(workloadInstanceRepoContent).contains(EXPECTED_CLUSTER_CONNECTION_INFO_FILE_NAME);
        }
    }

    @Test
    @Ignore
    @Description("Post workload instance dto that already exists on git repo.")
    public void postSameWorkloadInstanceTwice() {
        ResponseEntity<HashMap<String, Object>> initialCommit = createWorkloadInstance(WORKLOAD_INSTANCE_WITHOUT_ANY_EXTRA_PARAMS_JSON_PATH,
                                                                                       null,
                                                                                       null);
        assertThat(initialCommit.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        try {
            createWorkloadInstance(WORKLOAD_INSTANCE_WITHOUT_ANY_EXTRA_PARAMS_JSON_PATH, null, null);
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getMessage();
            assertThat(errorMessage).isNotEmpty();
            assertThat(errorMessage).contains("Workload instance name unique must be unique");
        }
    }


    @DataProvider(name = "workloadInstanceRequestData")
    private Object[][] workloadInstanceRequestData() {
        return new Object[][] {
            new Object[] {
                null, null //passing workloadInstancePostRequestDto (only required field)
            }, new Object[] {
                VALUES_PATH, null //passing workloadInstancePostRequestDto + values
            }, new Object[] {
                null, CLUSTER_CONNECTION_INFO_PATH //passing workloadInstancePostRequestDto + clusterConnectionInfo
            }, new Object[] {
                VALUES_PATH, CLUSTER_CONNECTION_INFO_PATH //passing workloadInstancePostRequestDto + values + clusterConnectionInfo
            }
        };
    }

    private static void prepareWorkloadInstanceJson(String workloadInstanceName) {
        Setup.execute(String.format("sed -i '/workloadInstanceName/c\\  \\\"workloadInstanceName\\\" : \\\"%s\\\",'  %s",
                                    workloadInstanceName, WORKLOAD_INSTANCE_JSON_PATH), BASH_EXECUTION_TIMEOUT);
    }

    private static void setRandomWorkloadInstanceName() {
        workloadInstanceName = "test-" + RandomGenerator.generateString(8, RandomGenerator.Mode.ALPHANUMERIC);
    }
}