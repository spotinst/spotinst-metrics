package com.spotinst.metrics.commons.configuration;

import jakarta.validation.constraints.NotEmpty;

/**
 * Created by aharontwizer on 7/18/15.
 */
public class ServicesConfig {

    @NotEmpty
    private String dbServiceUrl;

    @NotEmpty
    private String umsServiceUrl;

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
