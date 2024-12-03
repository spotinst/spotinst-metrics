package com.spotinst.metrics.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.commons.response.api.items.IServiceResponseItem;

import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiDimensionsValuesResponse implements IServiceResponseItem {
    private Map<String, Set<Object>> dimensions;

    public ApiDimensionsValuesResponse() {
    }

    public Map<String, Set<Object>> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, Set<Object>> dimensions) {
        this.dimensions = dimensions;
    }
}
