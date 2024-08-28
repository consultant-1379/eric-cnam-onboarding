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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePutRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;
import com.ericsson.oss.management.onboarding.presentation.services.workloadinstance.WorkloadInstanceService;

@ActiveProfiles("test")
@SpringBootTest(classes = WorkloadInstancesController.class)
class WorkloadInstancesControllerTest {

    @Autowired
    private WorkloadInstancesController controller;
    @MockBean
    private WorkloadInstanceService instanceService;

    @Test
    void shouldReturnAcceptedWhenCreate() {
        //Init
        WorkloadInstanceResponseDto dto = new WorkloadInstanceResponseDto();
        dto.setUrl("testUrl");

        //Test method
        HttpStatus result = controller
                .workloadInstancesPost(new WorkloadInstancePostRequestDto(), null, null)
                .getStatusCode();

        //Verify
        assertThat(result).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldReturnAcceptedWhenUpdate() {
        //Init
        WorkloadInstanceResponseDto dto = new WorkloadInstanceResponseDto();
        dto.setUrl("testUrl");

        //Test method
        HttpStatus result = controller
                .workloadInstancesPut(new WorkloadInstancePutRequestDto(), null)
                .getStatusCode();

        //Verify
        assertThat(result).isEqualTo(HttpStatus.ACCEPTED);
    }
}
