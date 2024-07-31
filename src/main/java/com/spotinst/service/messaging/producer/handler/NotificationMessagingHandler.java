package com.spotinst.service.messaging.producer.handler;

import com.spotinst.dropwizard.bl.notification.INotifiable;
import com.spotinst.dropwizard.bl.notification.INotificationHandler;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.messaging.clients.producer.IMessagingProducer;
import com.spotinst.messaging.model.InternalMessageEnvelope;
import com.spotinst.service.messaging.model.notification.SqsInternalNotification;
import com.spotinst.service.messaging.topics.SampleMessageTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spotinst.service.messaging.producer.utils.MessageContextUtil.create;

/**
 * Created by zachi.nachshon on 16/10/2018.
 */
public class NotificationMessagingHandler implements INotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMessagingHandler.class);

    private IMessagingProducer sqsProducer;

    public NotificationMessagingHandler(IMessagingProducer sqsProducer) {
        this.sqsProducer = sqsProducer;
    }

    public boolean publish(INotifiable data) throws DalException {
        boolean retVal = false;
        if(sqsProducer != null) {
            InternalMessageEnvelope messageEnvelope
                    = InternalMessageEnvelope.newBuilder()
                                             .setEntity(new SqsInternalNotification(data))
                                             .setTopic(SampleMessageTopics.InternalNotification)
                                             .setContext(create())
                                             .build();

            sqsProducer.publishMessage(SampleMessageTopics.InternalNotification, messageEnvelope);
            retVal = true;
        } else {
            LOGGER.error("Messaging producer wasn't initialized, cannot publish notification");
        }
        return retVal;
    }
}
