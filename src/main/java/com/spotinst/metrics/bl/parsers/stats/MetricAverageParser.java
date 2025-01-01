package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_AVERAGE_NAME;

public class MetricAverageParser implements IMetricStatParser {
    @Override
    public ElasticMetricStatistics parse(Aggregations aggregation) {
        ElasticMetricStatistics retVal  = null;
        ParsedSimpleValue       esStats = aggregation.get(AGG_METRIC_AVERAGE_NAME);

        if (esStats != null) {
            retVal = new ElasticMetricStatistics();
            retVal.setAverage(roundFix(esStats.value(), 10));
        }

        return retVal;
    }
}
