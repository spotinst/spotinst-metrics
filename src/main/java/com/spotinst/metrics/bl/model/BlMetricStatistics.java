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

    public BlMetricStatistics() {
    }

    public BlMetricStatistics(Double count, Double sum, Double average, Double minimum, Double maximum) {
        this.count = count;
        this.sum = sum;
        this.average = average;
        this.minimum = minimum;
        this.maximum = maximum;
    }

}
