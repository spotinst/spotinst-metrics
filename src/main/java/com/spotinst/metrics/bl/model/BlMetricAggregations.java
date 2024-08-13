package com.spotinst.metrics.bl.model;

import java.util.List;

public class BlMetricAggregations {
    private List<BlMetricDimension> dimensions;
    private List<BlMetricDatapoint> datapoints;

    public BlMetricAggregations() {
    }

    public List<BlMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<BlMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<BlMetricDatapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<BlMetricDatapoint> datapoints) {
        this.datapoints = datapoints;
    }
}
