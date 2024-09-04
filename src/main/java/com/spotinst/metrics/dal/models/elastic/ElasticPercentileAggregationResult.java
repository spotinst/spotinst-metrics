package com.spotinst.metrics.dal.models.elastic;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ElasticPercentileAggregationResult {
    //region Members
    private String  name;
    private Integer count;
    private Double  percentile;
    //endregion

    //region Constructors
    public ElasticPercentileAggregationResult() {
    }

    public ElasticPercentileAggregationResult(String name, Double percentile, Integer count) {
        this.name = name;
        this.count = count;
        this.percentile = percentile;
    }
}
