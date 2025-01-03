package com.spotinst.service.commons.converters;

import com.spotinst.commons.converters.BaseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public class CommonConverter extends BaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonConverter.class);

    private static final String ENTITY_TYPE = "Common";

    CommonConverter() {}

    // region toApi
    // endregion

    // region toBl
    // endregion

    // region toDb
    // endregion


    // region Register Converters
    // endregion

    // region Register Mappings
    // endregion

    // Mapping Registration
    @Override
    protected String entityType() {
        return ENTITY_TYPE;
    }

    @Override
    protected void registerConvertersInner() {

        // In here we should register properties that differ in their types and how to map them
        // Example: property named 'actionGroup' which is of type BlActionGroup on BlXXX and String on DbXXX

    }

    @Override
    protected void registerMappingsInner() {

        // In here we should register class mappings that built on top of pre-defined converters
        // Example: BlXXX should be mapped to DbXXX with special converter for 'actionGroup' property

    }
    // endregion
}
