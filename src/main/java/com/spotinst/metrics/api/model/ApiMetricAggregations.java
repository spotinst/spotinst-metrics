package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricAggregations {
    private List<ApiMetricDimension> dimensions;
    private List<ApiMetricDatapoint> datapoints;

    public ApiMetricAggregations() {
    }

    public List<ApiMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ApiMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<ApiMetricDatapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<ApiMetricDatapoint> datapoints) {
        this.datapoints = datapoints;
    }
}
