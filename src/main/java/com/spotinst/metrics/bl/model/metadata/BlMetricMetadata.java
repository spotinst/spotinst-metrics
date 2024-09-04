package com.spotinst.metrics.bl.model.metadata;

import com.spotinst.metrics.bl.model.BlMetric;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BlMetricMetadata {
    private BlNamespaceDimensionPair namespaceDimensionPair;
    private List<BlMetric>           metrics;

    public BlMetricMetadata() {}

    public BlMetricMetadata(BlNamespaceDimensionPair namespaceDimensionPair, List<BlMetric> metrics) {
        this.namespaceDimensionPair = namespaceDimensionPair;
        this.metrics = metrics;
    }

}