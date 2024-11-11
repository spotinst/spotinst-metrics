package com.spotinst.metrics.bl.parsers;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.Map;

public interface IMetricAggregationParser {
    String ORIGIN_AGG = "ORIGIN_AGG";

    Map<AggCompositeKey, Aggregations> parse(Map<AggCompositeKey, Aggregations> aggsMapByKeys,
                                             Map<String, ElasticMetricAggregations> byRefResultMap);

    String getAggregationName();
}
