package com.spotinst.metrics.bl.model;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlPercentileAggregationResult {
    //region Members
    private String  name;
    private Integer count;
    private Double  percentile;
    //endregion
}