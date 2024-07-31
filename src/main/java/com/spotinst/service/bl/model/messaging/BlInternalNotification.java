package com.spotinst.service.bl.model.messaging;

import jakarta.validation.constraints.NotNull;

public class BlInternalNotification {

    @NotNull
    private String subject;

    @NotNull
    private String message;

    private String level;

    private String resourceId;

    public BlInternalNotification() {}

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
