package com.spotinst.service;

import com.spotinst.commons.mapper.entities.registration.BaseMappingRegistration;
import com.spotinst.service.commons.converters.Converters;

/**
 * Created by zachi.nachshon on 7/26/17.
 */
public class ServiceMappingRegistration extends BaseMappingRegistration {

    @Override
    protected void registerEntitiesConverters() {
        Converters.Enum.registerConverters();
        Converters.Common.registerConverters();
        Converters.Dummy.registerConverters();
        Converters.Notification.registerConverters();
    }

    @Override
    protected void registerEntitiesMappings() {
        Converters.Enum.registerMappings();
        Converters.Common.registerMappings();
        Converters.Dummy.registerMappings();
        Converters.Notification.registerMappings();
    }
}
