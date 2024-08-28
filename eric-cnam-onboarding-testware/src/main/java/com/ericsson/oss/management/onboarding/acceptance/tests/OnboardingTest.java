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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.CSAR_ARCHIVE;
import static com.ericsson.oss.management.onboarding.acceptance.utils.Constants.CSAR_PATH;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.ericsson.oss.management.onboarding.acceptance.steps.Operations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.ericsson.oss.management.onboarding.acceptance.steps.Setup;

import io.qameta.allure.Description;

public class OnboardingTest {

    //URLS
    private static final String DOMAIN = "localhost:5000";
    private static final String HELMFILE_URL = DOMAIN + "/eric-bss-bam/eric-bss-bam-helmfile:3.6.0_14";

    //PATHS
    private static final String TEMP_DIR_WITH_RESOURCES_PATH = "tempUnpackedCsarContent";
    private static final String TEMPLATES_IN_DEFINITIONS_FOLDER = "/Definitions/OtherTemplates";
    private static final String TEMPLATES_PATH = TEMP_DIR_WITH_RESOURCES_PATH + TEMPLATES_IN_DEFINITIONS_FOLDER;
    private static final String HELM_CHARTS_PATH = TEMP_DIR_WITH_RESOURCES_PATH + "/helm-charts";
    private static final String HELM_CHARTS_TGZ_PATH = TEMP_DIR_WITH_RESOURCES_PATH + TEMPLATES_IN_DEFINITIONS_FOLDER + "/helm-charts.tgz";
    private static final String EMPTY_CSAR_PATH = "src/main/resources/testData/empty.csar";

    //OTHER
    private static final String UNZIP = "unzip %s -d %s";
    private static final String UNTAR = "tar -xf %s -C %s";
    private static final String LS_COMMAND = "ls ";
    private static final int TEST_TIMEOUT = 1;

    @AfterTest
    public static void clean() {
        Setup.execute("rm -rf " + TEMP_DIR_WITH_RESOURCES_PATH, TEST_TIMEOUT);
    }

    @Test
    @Description("Onboard csar sunny day scenario.")
    public void positiveOnboarding() {

        //Post valid CSAR and verify status code
        ResponseEntity<HashMap<String, Object>> responseEntity = postCsarAndVerifyStatusCode(CSAR_ARCHIVE, CSAR_PATH, HttpStatus.CREATED);

        //Unpack sent CSAR
        Setup.execute(String.format(UNZIP, CSAR_PATH, TEMP_DIR_WITH_RESOURCES_PATH), TEST_TIMEOUT);
        String templates = Setup.execute(LS_COMMAND + TEMPLATES_PATH, TEST_TIMEOUT);
        assertThat(templates.contains("helm-charts.tgz")).isTrue();
        assertThat(templates.contains("eric-bss-bam-helmfile-3.6.0+14.tgz")).isTrue();
        Setup.execute(String.format(UNTAR, HELM_CHARTS_TGZ_PATH, TEMP_DIR_WITH_RESOURCES_PATH), TEST_TIMEOUT);
        List<String> helmChartsContent =
                Arrays.asList(Setup.execute(LS_COMMAND + HELM_CHARTS_PATH, TEST_TIMEOUT).split("\\s+"));

        helmChartsContent.replaceAll(tgzName -> getResourceUrl(getHelmChartName(tgzName)));

        //Verify response body
        assertThat(responseEntity.getBody())
                .isNotNull()
                .hasSize(2)
                .containsKey("helmfileUrl")
                .containsEntry("helmChartUrls", HELMFILE_URL);

        assertThat(responseEntity.getBody()).extractingByKey("helmChartUrls")
                .asList()
                .hasSize(helmChartsContent.size())
                .containsAll(helmChartsContent);
    }

    @Test(dataProvider = "negativeOnboardingData")
    @Description("Negative onboarding cases.")
    public void negativeOnboarding(String bodyKeyName, String value, HttpStatus expectedStatus) {
        try {
            postCsarAndVerifyStatusCode(bodyKeyName, value, expectedStatus);
        } catch (HttpClientErrorException e) {
            if (expectedStatus == BAD_REQUEST) {
                return;
            }
            assertThat(e).isNotNull();
            assertThat(e.getMessage()).isNotNull()
                    .contains("\"{\"type\":\"Bad Request\",\"title\":\"InvalidFileException\"");
        }
    }

    @DataProvider(name = "negativeOnboardingData")
    private Object[][] negativeOnboardingData() {
        return new Object[][] {
            new Object[] {
                "invalidKeyName", CSAR_PATH, BAD_REQUEST
                }, new Object[] {
                    CSAR_ARCHIVE, EMPTY_CSAR_PATH, UNSUPPORTED_MEDIA_TYPE
                }
        };
    }

    private ResponseEntity<HashMap<String, Object>> postCsarAndVerifyStatusCode(String keyName,
                                                                                String csarPath,
                                                                                HttpStatus expectedStatusCode) {
        //Send Onboard request
        ResponseEntity<HashMap<String, Object>> responseEntity = Operations.postCsar(keyName, csarPath);

        //Verify response status code
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatusCode);
        return responseEntity;
    }

    @SuppressWarnings("java:S5852")
    private String getHelmChartName(String tgzName) {
        String[] splitted = tgzName.split("-");
        var version = Pattern.compile("((\\d+)\\.+)\\d\\+\\d+\\.(tgz)");
        var stringBuffer = new StringBuilder();
        for (String element : splitted) {
            var matcher = version.matcher(element);
            if (matcher.find()) {
                stringBuffer.append("helmchart:").append(element.replace("+", "_").split(".tgz")[0]);
            } else {
                stringBuffer.append(element).append("-");
            }
        }
        return stringBuffer.toString();
    }

    private String getResourceUrl(String helmChartName) {
        String vendor = helmChartName.split("-helmchart:")[0];
        return String.format("%s/%s/%s", DOMAIN, vendor, helmChartName);
    }
}