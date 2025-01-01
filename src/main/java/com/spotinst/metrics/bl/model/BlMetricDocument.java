package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BlMetricDocument {
    private String                  accountId;
    private String                  namespace;
    private String                  timestamp;
    private List<BlMetricDimension> dimensions;
    private List<BlMetric>          metrics;

    public BlMetricDocument() {
    }
}
