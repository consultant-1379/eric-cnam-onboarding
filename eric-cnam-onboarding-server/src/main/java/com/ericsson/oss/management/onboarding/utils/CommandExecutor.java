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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.models.CommandResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommandExecutor {

    private static final Pattern NON_ASCII_CHARS = Pattern.compile("[^\\p{ASCII}]");
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "<none>").toLowerCase().contains("windows");

    /**
     * Execute any commands in bash
     *
     * @param command to run
     * @param timeout of command to execute
     * @return response of execution
     */
    public CommandResponse execute(String command, int timeout) {
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

            final boolean commandTimedOut = process.waitFor(timeout, TimeUnit.MINUTES);
            if (!commandTimedOut) {
                log.error("Command :: {} took more than : {} minutes", String.join(" ", completeCommand), timeout);
                return parseProcessOutput(process, -1);
            }
            return parseProcessOutput(process, process.exitValue());
        } catch (IOException e) {
            log.error("Failed to run process due to {}", e.getMessage());
            throw new InternalRuntimeException(e);
        } catch (InterruptedException e) {
            log.error("Failed to complete process due to {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new InternalRuntimeException(e);
        } finally {
            if (process != null && process.isAlive())
                process.destroy();
        }
    }

    private CommandResponse parseProcessOutput(Process process, int exitCode) throws IOException {
        var commandResponse = new CommandResponse();
        commandResponse.setExitCode(exitCode);

        try (var br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = br.lines().map(String::trim)
                    .collect(Collectors.joining(System.lineSeparator()));
            var m = NON_ASCII_CHARS.matcher(output);
            output = m.replaceAll("");

            commandResponse.setOutput(output);
        }
        log.info("CommandResponse :: {} ", commandResponse);
        return commandResponse;
    }

}
