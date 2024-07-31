package com.spotinst.service.api.models.dummy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDummyUpdateRequest {

    @Valid
    private ApiDummyUpdateInner dummy;

    public ApiDummyUpdateInner getDummy() {
        return dummy;
    }

    public void setDummy(ApiDummyUpdateInner dummy) {
        this.dummy = dummy;
    }
}
