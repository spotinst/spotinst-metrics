package com.spotinst.metrics.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by zachi.nachshon on 1/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDimensionsValuesRequest {

    @NotNull
    private String namespace;

    @Size(min = 1)
    private List<String> dimensionKeys;

    public ApiDimensionsValuesRequest() {}

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(List<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }
}
