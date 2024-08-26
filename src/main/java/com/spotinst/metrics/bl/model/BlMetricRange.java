package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricRange {
    private Double minimum;
    private Double maximum;
    private Double gap;

    public BlMetricRange() {}

    @Override
    public String toString() {
        return "BlMetricRange{" + "minimum=" + minimum + ", maximum=" + maximum + ", gap=" + gap + '}';
    }
}
