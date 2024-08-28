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

import com.ericsson.oss.management.onboarding.ApplicationServer;
import com.ericsson.oss.management.onboarding.presentation.services.coordinator.CoordinatorService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest(classes = ApplicationServer.class)
public class PostOnboardingNegativeBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CoordinatorService coordinatorService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }
}
