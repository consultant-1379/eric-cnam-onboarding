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

import java.util.Optional;
import java.util.regex.Pattern;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidInputException;

/**
 * Validate workload instance
 */
public final class WorkloadInstanceValidator {

    private WorkloadInstanceValidator() {
    }

    /**
     * Template requirements:
     * contain at most 63 characters
     * contain only lowercase alphanumeric characters or '-'
     * start with an alphanumeric character
     * end with an alphanumeric character
     */
    private static final Pattern STATE_PATTERN_EXPRESSION = Pattern.compile("^[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$");
    private static final Pattern WORKLOAD_INSTANCE_NAME_PATTERN_EXPRESSION = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");

    /**
     * Validate workload instance
     *
     * @param requestDto will be validate
     */
    public static void validate(WorkloadInstancePostRequestDto requestDto) {
        var errors = new StringBuilder();

        String name = requestDto.getWorkloadInstanceName();
        validate(WORKLOAD_INSTANCE_NAME_PATTERN_EXPRESSION, name, "WorkloadInstanceName %s is invalid", errors);

        String namespace = requestDto.getNamespace();
        validate(STATE_PATTERN_EXPRESSION, namespace, "Namespace %s is invalid", errors);

        Optional<String> crdNamespace = Optional.ofNullable(requestDto.getCrdNamespace());
        crdNamespace.ifPresent(crdNamespaceGet -> validate(STATE_PATTERN_EXPRESSION, crdNamespaceGet, "CrdNamespace %s is invalid", errors));

        if (errors.length() != 0) {
            throw new InvalidInputException(errors.toString());
        }
    }

    private static void validate(Pattern pattern, String toBeValidated, String message, StringBuilder errors) {
        boolean isValid = ValidationUtils.matchByPattern(pattern, toBeValidated);
        if (!isValid) {
            errors.append(String.format(message, toBeValidated)).append("\n");
        }
    }
}
