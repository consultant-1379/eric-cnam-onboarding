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

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.management.onboarding.presentation.controllers.HealthController;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@ActiveProfiles("test")
@SpringBootTest(classes = HealthController.class)
public class ApiHealthBase {

    @InjectMocks
    private HealthController controller;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(controller);
    }
}
