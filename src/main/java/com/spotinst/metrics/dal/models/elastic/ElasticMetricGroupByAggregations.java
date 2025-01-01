package com.spotinst.metrics.dal.models.elastic;

import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import java.util.List;

public class ElasticMetricGroupByAggregations {
    List<TermsAggregationBuilder> groupByAggsOrder;

    public ElasticMetricGroupByAggregations(List<TermsAggregationBuilder> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }

    public List<TermsAggregationBuilder> getGroupByAggsOrder() {
        return groupByAggsOrder;
    }

    public void setGroupByAggsOrder(List<TermsAggregationBuilder> groupByAggsOrder) {
        this.groupByAggsOrder = groupByAggsOrder;
    }
}
