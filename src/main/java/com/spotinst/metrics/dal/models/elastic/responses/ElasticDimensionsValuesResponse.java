package com.spotinst.metrics.dal.models.elastic.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class ElasticDimensionsValuesResponse {
    private Map<String, Set<Object>> dimensions;

    public ElasticDimensionsValuesResponse() {
    }
}
