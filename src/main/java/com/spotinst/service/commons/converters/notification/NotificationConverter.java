package com.spotinst.service.commons.converters.notification;

import com.spotinst.commons.converters.BaseConverter;
import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.service.api.models.messaging.ApiInternalNotification;
import com.spotinst.service.commons.converters.CommonConverter;
import com.spotinst.service.messaging.model.notification.SqsInternalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zachi.nachshon on 16/09/2018.
 */
public class NotificationConverter extends BaseConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonConverter.class);

    public static final String ENTITY_TYPE = "Notification";

    public NotificationConverter() {}

    // Mapping Registration
    @Override
    protected String entityType() {
        return ENTITY_TYPE;
    }

    // region API
    // endregion

    // region BL
    // endregion

    // region DAL
    // endregion

    // region SQS
    public SqsInternalNotification toSqs(ApiInternalNotification entity) {
        SqsInternalNotification retVal = EntitiesMapper.instance.mapType(entity, SqsInternalNotification.class);
        return retVal;
    }
    // endregion

    @Override
    protected void registerConvertersInner() {

    }

    @Override
    protected void registerMappingsInner() {

    }
    // endregion
}