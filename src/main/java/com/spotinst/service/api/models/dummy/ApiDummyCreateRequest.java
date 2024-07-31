package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyCreateRequest {

    @Valid
    private ApiDummyCreateInner dummy;

    public ApiDummyCreateInner getDummy() {
        return dummy;
    }

    public void setDummy(ApiDummyCreateInner dummy) {
        this.dummy = dummy;
    }
}
