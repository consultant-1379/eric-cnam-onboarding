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
package com.ericsson.oss.management.onboarding.acceptance.utils;

public final class Constants {

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";

    public static final String SERVICE_IP = ServiceProperties.getProperty("serviceIp");
    public static final String SERVICE_PORT = ServiceProperties.getProperty("servicePort");
    public static final String GIT_REPO_PORT = "8086";
    public static final String ONBOARDING_URL = "http://%s:%s/cnonb/v1/onboarding";
    public static final String WORKLOAD_INSTANCES_URL = "http://%s:%s/cnonb/v1/workload_instances";
    public static final String CSAR_ARCHIVE = "csarArchive";
    public static final String CSAR_PATH = "src/main/resources/testData/tiny-eric-bss-bam-helmfile-3.6.0+14.csar";


    private Constants() {
    }
}