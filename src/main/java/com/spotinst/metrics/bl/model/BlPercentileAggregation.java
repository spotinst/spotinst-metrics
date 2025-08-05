package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlPercentileAggregation {
    //region Members
    private String aggName;
    private String metricName;
    private Double percentile;
    //endregion
}
