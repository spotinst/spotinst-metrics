package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricAggregations {
    private List<ApiMetricDimension> dimensions;
    private List<ApiMetricDatapoint> datapoints;

    public ApiMetricAggregations() {
    }
}
