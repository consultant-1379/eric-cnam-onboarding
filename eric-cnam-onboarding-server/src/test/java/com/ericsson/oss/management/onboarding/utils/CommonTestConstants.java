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

public final class CommonTestConstants {

    private CommonTestConstants() {
    }

    public static final String WORKLOAD_INSTANCE_NAME = "workloadinstancename";
    public static final String NAMESPACE = "namespace";
    public static final String GLOBAL_CRD_NAMESPACE_VALUE = "eric-crd-ns";
    public static final String CLUSTER_CONNECTION_INFO_YAML = "clusterConnectionInfo.yaml";
    public static final String CLUSTER_CONNECTION_INFO = "cluster";
    public static final String VALUES = "values";
    public static final String VALUES_YAML = "values.yaml";
    public static final String CLUSTER_CONNECTION_INFO_CONFIG = "cluster.config";
    public static final String HELM_SOURCE_URL = "localhost:5000/eric-bss-bam-helmfile-3.6.0+14";
    public static final String WORKLOAD_INSTANCE_JSON = "workloadinstancename.json";

    public static final String WORKLOAD_INSTANCE_JSON_CONTENT = "{\"workloadInstanceName\":\"workloadinstancename\",\"namespace\":\"namespace\","
            + "\"crdNamespace\":\"eric-crd-ns\",\"timeout\":null,\"helmSourceUrl\":\"localhost:5000/eric-bss-bam-helmfile-3"
            + ".6.0+14\",\"additionalParameters\":null}";

    public static final String CLUSTER_CONFIG_CONTENT =
            "apiVersion: v1\n"
                    + "kind: Config\n"
                    + "clusters:\n"
                    + "  - name: \"hahn117\"\n"
                    + "    crdNamespace: eric-crd-ns\n"
                    + "    cluster:\n"
                    + "      server: \"https://mocha.rnd.gic.ericsson.se/k8s/clusters/c-mdw5r\"\n"
                    + "\n"
                    + "users:\n"
                    + "  - name: \"hahn117\"\n"
                    + "    user:\n"
                    + "      token: \"\"\n"
                    + "\n"
                    + "\n"
                    + "contexts:\n"
                    + "  - name: \"hahn117\"\n"
                    + "    context:\n"
                    + "      user: \"hahn117\"\n"
                    + "      cluster: \"hahn117\"\n"
                    + "\n"
                    + "current-context: \"hahn117\"";

    public static final String VALUES_CONTENT =
            "global:\n"
                    + "  crd:\n"
                    + "    enabled: true\n"
                    + "\n"
                    + "cn-am-test-app-a:\n"
                    + "  enabled: true\n"
                    + "  fuu: bar\n"
                    + "  name: cn-am-test-app-a\n"
                    + "\n"
                    + "cn-am-test-app-b:\n"
                    + "  enabled: true\n"
                    + "  fuu: bar\n"
                    + "  name: cn-am-test-app-b\n"
                    + "\n"
                    + "cn-am-test-app-c:\n"
                    + "  enabled: false\n"
                    + "  fuu: bar\n"
                    + "  name: cn-am-test-app-c\n"
                    + "\n"
                    + "cn-am-test-crd:\n"
                    + "  enabled: false\n"
                    + "  fuu: bar";
}
