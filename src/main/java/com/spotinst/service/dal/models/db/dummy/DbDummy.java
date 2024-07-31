package com.spotinst.service.dal.models.db.dummy;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;

import java.util.List;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.FILTER_NAME_PARTIAL_UPDATE;

/**
 * Created by aharontwizer on 7/21/15.
 */
@JsonFilter(FILTER_NAME_PARTIAL_UPDATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbDummy extends PartialUpdateEntity {
    private String        id;
    private String        name;
    private String        type;
//    private Map<String, Double> config;
    private List<Integer> endpointData;
    private String        protocol;
    private Integer       port;

    public DbDummy() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        touch("type");
        this.type = type;
    }

    //    public Map<String, Double> getConfig() {
    //        return config;
    //    }
    //
    //    public void setConfig(Map<String, Double> config) {
    //        touch("config");
    //        this.config = config;
    //    }

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
}
