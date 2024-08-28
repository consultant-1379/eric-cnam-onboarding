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

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

public final class GitVersioningUtils {
    private GitVersioningUtils() {}

    private static final String DASH = "-";

    public static String createNextVersionForWorkloadInstance(List<String> versionList, String workloadInstanceName) {
        var versionNamePattern = Pattern.compile(workloadInstanceName + DASH + "\\d+");
        int lastVersionIndex = versionList.stream()
                .filter(versionName -> versionNamePattern.matcher(versionName).find())
                .map(GitVersioningUtils::getVersionIndex)
                .max(Comparator.naturalOrder())
                .orElse(NumberUtils.INTEGER_ZERO);

        return workloadInstanceName + DASH + (lastVersionIndex + 1);
    }

    private static int getVersionIndex(String version) {
        int dashIndex = version.lastIndexOf(DASH);
        return Integer.parseInt(version.substring(dashIndex + 1));
    }
}
