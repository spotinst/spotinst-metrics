package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.elasticsearch.search.aggregations.Aggregations;

public interface IMetricStatParser {
    ElasticMetricStatistics parse(Aggregations aggregation);
}
