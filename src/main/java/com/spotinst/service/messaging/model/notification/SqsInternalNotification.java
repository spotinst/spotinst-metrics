package com.spotinst.service.messaging.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.dropwizard.bl.notification.INotifiable;
import com.spotinst.messaging.model.BaseMessagingEntity;

/**
 * Created by zachi.nachshon on 22/08/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SqsInternalNotification extends BaseMessagingEntity implements INotifiable {

    private String subject;
    private String message;
    private String level;
    private String resourceId;

    public SqsInternalNotification() {}

    public SqsInternalNotification(String subject,
                                   String message,
                                   String level) {
        this.subject = subject;
        this.message = message;
        this.level = level;
    }

    public SqsInternalNotification(INotifiable notifiable) {
        this.subject = notifiable.getSubject();
        this.message = notifiable.getMessage();
        this.level = notifiable.getLevel();
        this.resourceId = notifiable.getResourceId();
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}