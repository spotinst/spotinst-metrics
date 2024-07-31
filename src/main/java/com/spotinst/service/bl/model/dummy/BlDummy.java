package com.spotinst.service.bl.model.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;
import com.spotinst.service.commons.enums.DummyEnum;

import java.util.List;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlDummy extends PartialUpdateEntity {

    private String              id;
    private String              name;
    private DummyEnum           type;
    private List<BlDummyConfig> config;
    private List<Integer>       endpointData;
    private String              protocol;
    private Integer             port;

    public BlDummy() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        touch("id");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        touch("name");
        this.name = name;
    }

    public DummyEnum getType() {
        return type;
    }

    public void setType(DummyEnum type) {
        touch("type");
        this.type = type;
    }

    public List<Integer> getEndpointData() {
        return endpointData;
    }

    public void setEndpointData(List<Integer> endpointData) {
        touch("endpointData");
        this.endpointData = endpointData;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        touch("protocol");
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        touch("port");
        this.port = port;
    }

    public List<BlDummyConfig> getConfig() {
        return config;
    }

    public void setConfig(List<BlDummyConfig> config) {
        touch("config");
        this.config = config;
    }
}
