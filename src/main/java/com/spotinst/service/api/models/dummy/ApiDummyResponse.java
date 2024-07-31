package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.response.api.items.IServiceResponseItem;

import java.util.List;

/**
 * Created by zachi.nachshon on 2/12/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyResponse implements IServiceResponseItem {

    private String                 id;
    private String                 name;
    private String                 type;
    private List<ApiDummyConfig>   config;
    private List<Integer>          endpoints;
    private ApiDummyConnectionInfo connectionInfo;

    public ApiDummyResponse() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ApiDummyConfig> getConfig() {
        return config;
    }

    public void setConfig(List<ApiDummyConfig> config) {
        this.config = config;
    }

    public List<Integer> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Integer> endpoints) {
        this.endpoints = endpoints;
    }

    public ApiDummyConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ApiDummyConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}
