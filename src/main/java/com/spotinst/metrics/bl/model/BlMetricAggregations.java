package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BlMetricAggregations {
    private List<BlMetricDimension> dimensions;
    private List<BlMetricDatapoint> datapoints;

    public BlMetricAggregations() {
    }
}
