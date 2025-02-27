package com.spotinst.metrics.dal.models.elastic;

import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;

import java.util.List;

public class ElasticMetricGroupByAggregations {
    List<TermsAggregation> groupByAggsOrder;

    public ElasticMetricGroupByAggregations(List<TermsAggregation> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }

    public List<TermsAggregation> getGroupByAggsOrder() {
        return groupByAggsOrder;
    }

    public void setGroupByAggsOrder(List<TermsAggregation> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }
}
