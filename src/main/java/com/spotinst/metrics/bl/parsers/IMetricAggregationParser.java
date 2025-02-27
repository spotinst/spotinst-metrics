package com.spotinst.metrics.bl.parsers;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;

import java.util.Map;

public interface IMetricAggregationParser {
    String ORIGIN_AGG = "ORIGIN_AGG";

    Map<AggCompositeKey, Aggregate> parse(Map<AggCompositeKey, Aggregate> aggsMapByKeys,
                                          Map<String, ElasticMetricAggregations> byRefResultMap);

    String getAggregationName();
}
