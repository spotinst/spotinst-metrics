package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;

import java.util.Map;

public interface IMetricStatParser {
    ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap);
}
