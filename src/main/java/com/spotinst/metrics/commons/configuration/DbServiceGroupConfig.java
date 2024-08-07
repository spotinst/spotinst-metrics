package com.spotinst.metrics.commons.configuration;

import jakarta.validation.constraints.NotEmpty;

/**
 * Created by aharontwizer on 7/18/15.
 */
public class DbServiceGroupConfig {

    //region Members
    @NotEmpty
    private String name;

    @NotEmpty
    private String url;
    //endregion

    //region Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //endregion
}
