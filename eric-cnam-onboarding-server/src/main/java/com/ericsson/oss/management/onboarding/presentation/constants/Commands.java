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

package com.ericsson.oss.management.onboarding.presentation.constants;

public final class Commands {
    private Commands(){}

    public static final String UNZIP = "unzip %s -d %s";
    public static final String UNTAR = "tar -xf %s -C %s";
    public static final String SPACE = " ";
    public static final String SLASH = "/";
    public static final String ORAS_COMMAND = "oras";
    public static final String HELM_COMMAND = "helm";
    public static final String OCI_PREFIX = "oci://";
    public static final String PUSH = "push";
    public static final String CONFIG = "--config";
    public static final String INSECURE_FLAG = "--insecure";
    public static final int TIMEOUT = 5;
    public static final String ORAS_CONFIG = "/dev/null:application/vnd.acme.rocket.config";
    public static final String CHANGE_DIRECTORY = "cd";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String CHARTS = "charts/";

}