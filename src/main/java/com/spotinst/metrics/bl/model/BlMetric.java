package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetric {
    private String name;
    private Double value;
    private String unit;
    private Long   count;

    public BlMetric() {
    }
}
