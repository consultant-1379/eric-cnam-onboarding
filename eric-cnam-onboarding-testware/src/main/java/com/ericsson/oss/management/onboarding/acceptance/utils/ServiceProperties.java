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
package com.ericsson.oss.management.onboarding.acceptance.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public final class ServiceProperties {

    private static Properties properties;

    static {
        properties = new Properties();
        URL props = ClassLoader.getSystemResource("config.properties");
        try {
            properties.load(props.openStream());
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ServiceProperties() {
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getPropertyAsInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static boolean getPropertyAsBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}