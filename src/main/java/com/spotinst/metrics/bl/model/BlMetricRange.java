package com.spotinst.metrics.bl.model;

public class BlMetricRange {
    private Double minimum;
    private Double maximum;
    private Double gap;

    public BlMetricRange() {}

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

    @Override
    public String toString() {
        return "BlMetricRange{" + "minimum=" + minimum + ", maximum=" + maximum + ", gap=" + gap + '}';
    }
}
