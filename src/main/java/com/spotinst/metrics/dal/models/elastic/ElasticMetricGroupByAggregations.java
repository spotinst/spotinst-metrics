package com.spotinst.metrics.dal.models.elastic;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;

import java.util.List;

public class ElasticMetricGroupByAggregations {
    List<Aggregation.Builder> groupByAggsOrder;

    public ElasticMetricGroupByAggregations(List<Aggregation.Builder> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }

    public List<Aggregation.Builder> getGroupByAggsOrder() {
        return groupByAggsOrder;
    }

    public void setGroupByAggsOrder(List<Aggregation.Builder> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }
}
