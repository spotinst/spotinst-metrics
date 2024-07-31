package com.spotinst.service.commons.configuration;

import jakarta.validation.constraints.NotEmpty;

/**
 * Created by aharontwizer on 7/18/15.
 */
public class ServicesConfig {

    @NotEmpty
    private String eventsServiceUrl;

    @NotEmpty
    private String dbServiceUrl;

    @NotEmpty
    private String umsServiceUrl;

    public String getEventsServiceUrl() {
        return eventsServiceUrl;
    }

    public void setEventsServiceUrl(String eventsServiceUrl) {
        this.eventsServiceUrl = eventsServiceUrl;
    }

    public String getDbServiceUrl() {
        return dbServiceUrl;
    }

    public void setDbServiceUrl(String dbServiceUrl) {
        this.dbServiceUrl = dbServiceUrl;
    }

    public String getUmsServiceUrl() {
        return umsServiceUrl;
    }

    public void setUmsServiceUrl(String umsServiceUrl) {
        this.umsServiceUrl = umsServiceUrl;
    }
}
