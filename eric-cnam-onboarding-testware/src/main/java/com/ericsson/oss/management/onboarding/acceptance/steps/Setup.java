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

package com.ericsson.oss.management.onboarding.acceptance.steps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.ericsson.oss.management.onboarding.acceptance.exceptions.SetupException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Setup {
    private Setup(){}

    private static final boolean IS_WINDOWS = System
            .getProperty("os.name", "<none>")
            .toLowerCase()
            .contains("windows");

    public static String execute(String command, int timeout) {
        List<String> completeCommand = new ArrayList<>();

        if (IS_WINDOWS) {
            completeCommand.add("cmd.exe");
            completeCommand.add("/c");
        } else {
            completeCommand.add("bash");
            completeCommand.add("-c");
        }
        completeCommand.add(command);

        var pb = new ProcessBuilder(completeCommand);
        pb.redirectErrorStream(true);
        Process process = null;

        try {
            log.info("Executing {}", String.join(" ", pb.command()));
            process = pb.start();

            final boolean commandCompletedSuccessfully = process.waitFor(timeout, TimeUnit.MINUTES);
            if (!commandCompletedSuccessfully) {
                throw new SetupException("Failed to execute command, output: " + getOutput(process));
            }
            String output = getOutput(process);
            log.info("Execute output: " + output);
            return output;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SetupException("Failed to execute command due to " + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    private static String getOutput(Process process) throws IOException {
        try (var br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return br
                    .lines()
                    .map(String::trim)
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }
}