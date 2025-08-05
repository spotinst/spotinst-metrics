package com.spotinst.metrics.bl.model.response;

import com.spotinst.metrics.bl.model.BlMetricAggregations;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BlMetricStatisticsResponse {
    private List<BlMetricAggregations> aggregations;

    public BlMetricStatisticsResponse() {}

    public Boolean isEmpty() {
        return aggregations == null || aggregations.isEmpty();
    }
}
