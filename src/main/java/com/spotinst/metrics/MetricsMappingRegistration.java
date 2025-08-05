package com.spotinst.metrics;

import com.spotinst.commons.mapper.entities.registration.BaseMappingRegistration;
import com.spotinst.metrics.commons.converters.Converters;

/**
 * Created by zachi.nachshon on 7/26/17.
 */
public class MetricsMappingRegistration extends BaseMappingRegistration {

    @Override
    protected void registerEntitiesConverters() {
        Converters.Enum.registerConverters();
        Converters.Common.registerConverters();
    }

    @Override
    protected void registerEntitiesMappings() {
        Converters.Enum.registerMappings();
        Converters.Common.registerMappings();
    }
}
