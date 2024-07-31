package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyConfig extends PartialUpdateEntity {
    private String key;
    private String value;

    public ApiDummyConfig() {}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        touch("key");
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        touch("value");
        this.value = value;
    }
}
