package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;

public interface IMetricStatParser {
    ElasticMetricStatistics parse(Aggregate aggregate);
}
