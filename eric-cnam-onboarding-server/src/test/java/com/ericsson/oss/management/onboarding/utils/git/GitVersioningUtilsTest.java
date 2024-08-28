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
package com.ericsson.oss.management.onboarding.utils.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GitVersioningUtilsTest {

    private static final String WORKLOAD_INSTANCE_NAME = "test";
    private static final String WORKLOAD_INSTANCE_NAME_TWO = "test-two";
    private static final String DASH = "-";

    @Autowired
    private GitVersioningUtils gitVersioningUtils;

    @Test
    void shouldCreateNextVersionForWorkloadInstanceWhenSeveralWorkloadInstances() {
        int lastVersionIndex = 11;
        List<String> versionList = getVersionList(lastVersionIndex, WORKLOAD_INSTANCE_NAME);
        versionList.addAll(getVersionList(lastVersionIndex, WORKLOAD_INSTANCE_NAME_TWO));

        String result = GitVersioningUtils.createNextVersionForWorkloadInstance(versionList, WORKLOAD_INSTANCE_NAME);

        assertThat(result).isEqualTo(WORKLOAD_INSTANCE_NAME + DASH + (lastVersionIndex + 1));
    }

    @Test
    void shouldCreateFirstVersionForWorkloadInstance() {
        int lastVersionIndex = 0;
        List<String> versionList = getVersionList(lastVersionIndex, WORKLOAD_INSTANCE_NAME);
        String result = GitVersioningUtils.createNextVersionForWorkloadInstance(versionList, WORKLOAD_INSTANCE_NAME);

        assertThat(result).isEqualTo(WORKLOAD_INSTANCE_NAME + DASH + (lastVersionIndex + 1));
    }

    private List<String> getVersionList(int amount, String workloadInstanceName) {
        List<String> versionList = new ArrayList<>();
        for (int i = amount; i > 0; i--) {
            versionList.add(workloadInstanceName + DASH + i);
        }

        return versionList;
    }
}
