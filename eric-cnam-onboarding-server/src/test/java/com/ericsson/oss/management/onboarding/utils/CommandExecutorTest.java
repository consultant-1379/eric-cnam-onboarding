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
package com.ericsson.oss.management.onboarding.utils;

import com.ericsson.oss.management.onboarding.models.CommandResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { CommandExecutor.class })
class CommandExecutorTest {

    @Autowired
    private CommandExecutor commandExecutor;

    private static final String COMMAND = "echo 'test'; sleep 5";

    @Test
    void shouldExecuteCommandWithNegativeCodeWhenTimeoutExceeds() {
        CommandResponse response = commandExecutor.execute(COMMAND, 0);

        assertThat(response.getExitCode()).isEqualTo(-1);
    }

    @Test
    void shouldExecuteCommandWithPositiveCodeWhenTimeoutIsNotExceeds() {
        CommandResponse response = commandExecutor.execute(COMMAND, 2);

        assertThat(response.getOutput()).isEqualTo("test");
        assertThat(response.getExitCode()).isZero();
    }

}
