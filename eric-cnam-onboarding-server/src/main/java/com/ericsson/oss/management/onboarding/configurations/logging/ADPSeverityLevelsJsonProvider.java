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

package com.ericsson.oss.management.onboarding.configurations.logging;

import java.io.IOException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractPatternJsonProvider;
import net.logstash.logback.pattern.AbstractJsonPatternParser;
import net.logstash.logback.pattern.LoggingEventJsonPatternParser;

public class ADPSeverityLevelsJsonProvider extends AbstractPatternJsonProvider<ILoggingEvent> {

    private static final String SEVERITY_FIELD = "severity";
    private static final String DEBUG = "debug";
    private static final String INFO = "info";
    private static final String WARNING = "warning";
    private static final String ERROR = "error";

    @Override
    protected AbstractJsonPatternParser<ILoggingEvent> createParser(JsonFactory jsonFactory) {
        return new LoggingEventJsonPatternParser(context, jsonFactory);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent iLoggingEvent) throws IOException {
        switch (iLoggingEvent.getLevel().levelInt) {
            case Level.OFF_INT:
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
                generator.writeStringField(SEVERITY_FIELD, DEBUG);
                break;
            case Level.INFO_INT:
                generator.writeStringField(SEVERITY_FIELD, INFO);
                break;
            case Level.WARN_INT:
                generator.writeStringField(SEVERITY_FIELD, WARNING);
                break;
            case Level.ERROR_INT:
                generator.writeStringField(SEVERITY_FIELD, ERROR);
                break;
            default:
                generator.writeStringField(SEVERITY_FIELD, iLoggingEvent.getLevel().levelStr);
        }
        super.writeTo(generator, iLoggingEvent);
    }
}
