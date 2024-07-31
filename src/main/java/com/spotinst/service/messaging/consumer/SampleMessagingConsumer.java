package com.spotinst.service.messaging.consumer;

import com.spotinst.messaging.clients.consumer.sqs.SqsMessagingConsumer;
import com.spotinst.messaging.configuration.sqs.SqsMessagingConfiguration;
import com.spotinst.messaging.service.IMessageTopicHandlerMapping;

/**
 * Created by zachi.nachshon on 20/08/2018.
 */
public class SampleMessagingConsumer extends SqsMessagingConsumer {

    protected SampleMessagingConsumer(SqsMessagingConfiguration messagingConfiguration) {
        super(messagingConfiguration);
    }

    @Override
    protected IMessageTopicHandlerMapping getMessageTopicHandlerMapping() {
        return new SampleMessageTopicHandlerMapping();
    }
}