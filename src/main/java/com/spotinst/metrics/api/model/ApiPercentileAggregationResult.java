package com.spotinst.metrics.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiPercentileAggregationResult {
    //region Members
    private String  name;
    private Double  percentile;
    private Integer count;
    //endregion

    public ApiPercentileAggregationResult() {
    }
}