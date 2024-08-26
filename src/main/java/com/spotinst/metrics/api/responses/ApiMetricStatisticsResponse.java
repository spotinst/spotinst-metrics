package com.spotinst.metrics.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.commons.response.api.items.IServiceResponseItem;
import com.spotinst.metrics.api.model.ApiMetricAggregations;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricStatisticsResponse implements IServiceResponseItem {
    private List<ApiMetricAggregations> aggregations;

    public ApiMetricStatisticsResponse() {
    }
}
