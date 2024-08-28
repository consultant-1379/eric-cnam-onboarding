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

package com.ericsson.oss.management.onboarding.presentation.mappers;

import org.springframework.stereotype.Component;

import com.ericsson.oss.management.onboarding.models.OCIRegistryResponse;
import com.ericsson.oss.management.onboarding.utils.MapperUtils;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

@Component
public class CsarOnboardingResponseDtoMapper extends ConfigurableMapper {

    @Override
    protected void configure(MapperFactory factory) {
        MapperUtils.setDefaultSettings(factory, OCIRegistryResponse.class, CsarOnboardingResponseDtoMapper.class);
    }
}
