package com.spotinst.metrics.bl.model.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class BlDimensionsValuesResponse {
    private Map<String, Set<Object>> dimensions;

    public BlDimensionsValuesResponse() {
    }

    public Boolean isEmpty() {
        return dimensions == null || dimensions.isEmpty();
    }
}
