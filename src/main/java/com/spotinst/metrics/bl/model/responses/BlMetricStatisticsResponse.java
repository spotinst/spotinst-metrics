package com.spotinst.metrics.bl.model.responses;

import com.spotinst.metrics.bl.model.BlMetricAggregations;
import java.util.List;

public class BlMetricStatisticsResponse {
    private List<BlMetricAggregations> aggregations;

    public BlMetricStatisticsResponse() {}

    public List<BlMetricAggregations> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<BlMetricAggregations> aggregations) {
        this.aggregations = aggregations;
    }

    public Boolean isEmpty() {
        return aggregations == null || aggregations.isEmpty();
    }
}
