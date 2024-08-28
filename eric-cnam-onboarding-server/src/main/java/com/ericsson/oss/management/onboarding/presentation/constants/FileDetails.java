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

public final class FileDetails {

    private FileDetails(){}

    public static final String METADATA_YAML = "metadata.yaml";
    public static final String CHART_YAML = "Chart.yaml";
    public static final String VERSION_KEY = "version";
    public static final String NAME_KEY = "name";
    public static final String CHARTS_DIR = "/charts";
    public static final String MANIFEST_FILE = "manifest.json";
    public static final String DEFAULT_DOCKER_REGISTRY = "armdocker.rnd.ericsson.se";
    public static final String JSON_EXT = ".json";
    public static final String YAML_EXT = ".yaml";
    public static final String CONFIG_EXT = ".config";
    public static final String LOCAL_REPO_PATH = "/LocalRepository";
    public static final String LOCAL_REPO_GIT_PATH = "/LocalRepository/.git";
    public static final String HELMFILE = "-helmfile";

    public static final String CONTENT_TYPE = "text/plain";
    public static final String DOCKER_LAYER_MEDIA_TYPE_TAR = "application/vnd.docker.image.rootfs.diff.tar.gzip";
    public static final String DOCKER_LAYER_MEDIA_TYPE_JSON = "application/vnd.docker.container.image.v1+json";
    public static final String DOCKER_LAYER_CONTENT_TYPE = "application/vnd.docker.distribution.manifest.v2+json";

}
