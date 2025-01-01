package com.spotinst.metrics.dal.models.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticPercentileAggregation {
    //region Members
    private String aggName;
    private String metricName;
    private Double percentile;
    //endregion
}
