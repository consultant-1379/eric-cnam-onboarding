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

package com.ericsson.oss.management.onboarding.contracts.base;

import com.ericsson.oss.management.onboarding.api.model.WorkloadInstanceResponseDto;
import com.ericsson.oss.management.onboarding.presentation.controllers.WorkloadInstancesController;
import com.ericsson.oss.management.onboarding.presentation.services.workloadinstance.WorkloadInstanceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PutUpdateWorkloadPositiveBase {

    @InjectMocks
    private WorkloadInstancesController controller;
    @Mock
    private WorkloadInstanceService instanceService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(controller);

        WorkloadInstanceResponseDto dto = new WorkloadInstanceResponseDto();
        dto.setUrl("testUrl");
        when(instanceService.update(any(), any())).thenReturn(dto);
    }
}
