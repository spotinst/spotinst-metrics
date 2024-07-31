package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyConnectionInfo extends PartialUpdateEntity {
    private String  protocol;
    private Integer port;

    public ApiDummyConnectionInfo() {
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
