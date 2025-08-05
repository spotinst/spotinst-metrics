package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricDatapoint {
    private String             timestamp;
    private BlMetricStatistics statistics;

    public BlMetricDatapoint() {
    }
}
