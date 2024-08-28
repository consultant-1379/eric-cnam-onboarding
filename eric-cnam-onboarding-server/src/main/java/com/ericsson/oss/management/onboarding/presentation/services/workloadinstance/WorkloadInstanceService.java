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

package com.ericsson.oss.management.onboarding.presentation.services.workloadinstance;

import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePutRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;

/**
 * Working with WorkloadInstance
 */
public interface WorkloadInstanceService {

    /**
     * Take all the parts of a Post request and create a package with all this data for workload in GitRepo.
     *
     * @param requestDto
     * @param values
     * @param clusterConnectionInfo
     * @return WorkloadInstanceResponseDto dto with url to uploaded data
     */
    WorkloadInstanceResponseDto create(WorkloadInstancePostRequestDto requestDto, MultipartFile values, MultipartFile clusterConnectionInfo);

    /**
     * Take all the parts of a Put request and update a new version of the workload instance in GitRepo.
     *
     * @param requestDto
     * @param values
     * @return WorkloadInstanceResponseDto dto with url to updated data
     */
    WorkloadInstanceResponseDto update(WorkloadInstancePutRequestDto requestDto, MultipartFile values);
}
