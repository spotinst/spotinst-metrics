package com.spotinst.metrics.dal.models.elastic.responses;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;

import java.util.ArrayList;
import java.util.List;

public class ElasticMetricStatisticsResponse {
    private List<ElasticMetricAggregations> aggregations;

    public ElasticMetricStatisticsResponse() {
    }

    public List<ElasticMetricAggregations> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<ElasticMetricAggregations> aggregations) {
        this.aggregations = aggregations;
    }

    public void addMetricAggregation(ElasticMetricAggregations metricAggregations) {
        if(aggregations == null) {
            aggregations = new ArrayList<>();
        }

        aggregations.add(metricAggregations);
    }
}
