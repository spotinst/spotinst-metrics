package com.spotinst.service.messaging.consumer;

import com.spotinst.messaging.service.IConsumerHandler;
import com.spotinst.messaging.service.IMessageTopicHandlerMapping;
import com.spotinst.service.messaging.consumer.handler.sample.SampleConsumerHandler;
import com.spotinst.service.messaging.consumer.handler.sample.SampleOtherConsumerHandler;
import com.spotinst.service.messaging.topics.SampleMessageTopics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zachi.nachshon on 08/08/2018.
 */
public class SampleMessageTopicHandlerMapping extends HashMap<String, IConsumerHandler>
        implements IMessageTopicHandlerMapping {

    public SampleMessageTopicHandlerMapping() {
        initMapping();
    }

    /**
     * Mapping initialization should take place after service had been fully initialized.
     */
    private void initMapping() {
        this.put(SampleMessageTopics.SampleTopic.identifier(), new SampleConsumerHandler());
        this.put(SampleMessageTopics.SampleOtherTopic.identifier(), new SampleOtherConsumerHandler());
    }

    @Override
    public IConsumerHandler getConsumerHandler(String messageTopic) {
        return messageTopic != null ?
               this.get(messageTopic) :
               null;
    }

    @Override
    public List<String> getSupportedMessageTopics() {
        return new ArrayList<>(this.keySet());
    }

    @Override
    public boolean containsHandler(String messageTopic) {
        return this.containsKey(messageTopic);
    }
}
