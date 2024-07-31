package com.spotinst.service.messaging.topics;

import com.spotinst.messaging.clients.producer.IMessageTopic;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zachi.nachshon on 30/07/2018.
 */
public enum SampleMessageTopics implements IMessageTopic {

    InternalNotification("InternalNotification"),
    SampleTopic("SampleTopic"),
    SampleOtherTopic("SampleOtherTopic");

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleMessageTopics.class);

    private String name;

    private static Map<String, SampleMessageTopics> valuesMap;

    static {
        valuesMap = new HashMap<>();

        for(SampleMessageTopics messageTopic : SampleMessageTopics.values()) {
            valuesMap.put(messageTopic.name.toLowerCase(), messageTopic);
        }
    }

    SampleMessageTopics(String name) {
        this.name = name;
    }

    public static SampleMessageTopics fromName(String name) {
        SampleMessageTopics retVal = null;

        if(StringUtils.isNotEmpty(name)) {
            String nameLower = name.toLowerCase();

            if (valuesMap.containsKey(nameLower)) {
                retVal = valuesMap.get(nameLower);
            } else {
                String format = "Value [%s] is not part of the supported message topics";
                String errMsg = String.format(format, name);
                LOGGER.error(errMsg);
            }
        }

        return retVal;
    }

    @Override
    public String identifier() {
        return name;
    }
}