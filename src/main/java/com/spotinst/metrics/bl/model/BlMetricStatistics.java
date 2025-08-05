package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricStatistics {
    private Double count;
    private Double sum;
    private Double average;
    private Double minimum;
    private Double maximum;
}
