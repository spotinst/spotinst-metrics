package com.spotinst.service.bl.model.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlDummyConfig extends PartialUpdateEntity {
    private String key;
    private String value;

    public BlDummyConfig() {}

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
