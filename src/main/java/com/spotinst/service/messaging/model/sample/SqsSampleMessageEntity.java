package com.spotinst.service.messaging.model.sample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.messaging.model.BaseMessagingEntity;

/**
 * Created by zachi.nachshon on 22/08/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SqsSampleMessageEntity extends BaseMessagingEntity {

    private String content;

    public SqsSampleMessageEntity() {}

    public SqsSampleMessageEntity(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
