package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.InternalMin;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_MIN_NAME;

public class MetricMinParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Aggregations aggregation) {
        ElasticMetricStatistics retVal  = null;
        InternalMin             esStats = aggregation.get(AGG_METRIC_MIN_NAME);

        if (esStats != null) {
            retVal = new ElasticMetricStatistics();
            retVal.setMinimum(roundFix(esStats.getValue(), 10));
        }

        return retVal;
    }
}