package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricDimension {
    private String name;
    private String value;

    public BlMetricDimension() {
    }
}
