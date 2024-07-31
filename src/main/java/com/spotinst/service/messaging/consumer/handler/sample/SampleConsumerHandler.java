package com.spotinst.service.messaging.consumer.handler.sample;

import com.spotinst.messaging.model.MessageParams;
import com.spotinst.messaging.service.IConsumerHandler;
import com.spotinst.service.messaging.consumer.handler.BaseConsumerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zachi.nachshon on 08/08/2018.
 */
public class SampleConsumerHandler extends BaseConsumerHandler implements IConsumerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleConsumerHandler.class);

    public SampleConsumerHandler() {}

    @Override
    public Class<?> getEntityClass() {
        return null;
    }

    @Override public void handle(MessageParams messageParams) {
        LOGGER.info("Starting SampleConsumerHandler...");

        // Do something
        LOGGER.info("I'm here at SampleConsumerHandler.handle()");

        messageParams.getAcknowledger().ack();

        LOGGER.info("Finished SampleConsumerHandler");
    }
}
