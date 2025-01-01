package com.spotinst.metrics.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.commons.response.api.items.IServiceResponseItem;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiDimensionsValuesResponse implements IServiceResponseItem {
    private Map<String, Set<Object>> dimensions;

    public ApiDimensionsValuesResponse() {
    }
}
