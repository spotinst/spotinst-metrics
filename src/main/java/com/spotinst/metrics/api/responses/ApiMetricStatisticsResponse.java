package com.spotinst.metrics.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.commons.response.api.items.IServiceResponseItem;
import com.spotinst.metrics.api.model.ApiMetricAggregations;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricStatisticsResponse implements IServiceResponseItem {
    private List<ApiMetricAggregations> aggregations;

    public ApiMetricStatisticsResponse() {
    }

    public List<ApiMetricAggregations> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<ApiMetricAggregations> aggregations) {
        this.aggregations = aggregations;
    }
}
