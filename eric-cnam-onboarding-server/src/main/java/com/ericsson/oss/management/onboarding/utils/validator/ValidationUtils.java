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
package com.ericsson.oss.management.onboarding.utils.validator;

import java.util.regex.Pattern;

/**
 * Validate content by pattern
 */
public final class ValidationUtils {
    private ValidationUtils() {
    }

    /**
     * Match content by pattern
     *
     * @param pattern to check content
     * @param text content which will be checked
     * @return boolean result of checking
     */
    public static boolean matchByPattern(Pattern pattern, String text) {
        var matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
