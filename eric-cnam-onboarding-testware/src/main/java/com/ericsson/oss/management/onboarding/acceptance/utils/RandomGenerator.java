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

import java.security.SecureRandom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RandomGenerator {

    private RandomGenerator(){}

    public static String generateString(int length, Mode mode) {
        var buffer = new StringBuilder();
        var characters = "";

        switch (mode) {

            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyz";
                break;

            case ALPHANUMERIC:
                characters = "abcdefghijklmnopqrstuvwxyz1234567890";
                break;

            case NUMERIC:
                characters = "1234567890";
                break;

            default: break;
        }

        int charactersLength = characters.length();

        for (var i = 0; i < length; i++) {
            double index = new SecureRandom().nextDouble() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    }

    public static Integer generateInteger(int min, int max) {
        return new SecureRandom().nextInt(max - min) + min;
    }

    public static Long generateLong(long min, long max) {
        return min + (new SecureRandom().nextLong() * (max - min));
    }

    public static Float generateFloat(float min, float max) {
        return min + new SecureRandom().nextFloat() * (max - min);
    }

    public static Double generateDouble(double min, double max) {
        return min + new SecureRandom().nextDouble() * (max - min);
    }

    public enum Mode {
        ALPHA, ALPHANUMERIC, NUMERIC
    }
}