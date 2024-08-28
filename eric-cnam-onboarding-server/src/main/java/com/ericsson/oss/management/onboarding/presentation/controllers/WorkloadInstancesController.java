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

package com.ericsson.oss.management.onboarding.presentation.controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.oss.management.onboarding.api.WorkloadInstancesApi;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePutRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;
import com.ericsson.oss.management.onboarding.presentation.services.workloadinstance.WorkloadInstanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/cnonb/v1")
@RequiredArgsConstructor
public class WorkloadInstancesController implements WorkloadInstancesApi {

    private final WorkloadInstanceService workloadInstanceService;

    @Override
    public ResponseEntity<WorkloadInstanceResponseDto> workloadInstancesPost(
            @Valid WorkloadInstancePostRequestDto workloadInstancePostRequestDto,
            @Valid MultipartFile clusterConnectionInfo,
            @Valid MultipartFile values) {
        log.info("Received a POST request to create a workload instance with name {} in GitRepo",
                 workloadInstancePostRequestDto.getWorkloadInstanceName());
        WorkloadInstanceResponseDto responseDto = workloadInstanceService.create(workloadInstancePostRequestDto, values, clusterConnectionInfo);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<WorkloadInstanceResponseDto> workloadInstancesPut(
            @Valid WorkloadInstancePutRequestDto workloadInstancePutRequestDto,
            @Valid MultipartFile values) {
        log.info("Received a PUT request to update a workload instance with name {} in GitRepo",
                workloadInstancePutRequestDto.getWorkloadInstanceName());
        WorkloadInstanceResponseDto responseDto = workloadInstanceService.update(workloadInstancePutRequestDto, values);
        return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
    }
}