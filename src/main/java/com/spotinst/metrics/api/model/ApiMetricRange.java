package com.spotinst.metrics.api.model;

/**
 * Created by Tal.Geva on 07/08/2024.
 */
public class ApiMetricRange {
    private Double minimum;
    private Double maximum;
    private Double gap;

    public ApiMetricRange() {
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getGap() {
        return gap;
    }

    public void setGap(Double gap) {
        this.gap = gap;
    }
}
