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

import ma.glasnost.orika.MapperFactory;

public final class MapperUtils {
    private MapperUtils() {}

    public static <A, B> void setDefaultSettings(MapperFactory factory, Class<A> source, Class<B> target) {
        factory.classMap(source, target)
                .mapNulls(false)
                .mapNullsInReverse(false)
                .byDefault()
                .register();
    }
}
