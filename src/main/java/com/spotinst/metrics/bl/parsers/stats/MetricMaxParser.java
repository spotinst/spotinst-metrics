package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.InternalMax;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_MAX_NAME;

public class MetricMaxParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Aggregations aggregation) {
        ElasticMetricStatistics retVal  = null;
        InternalMax        esStats = aggregation.get(AGG_METRIC_MAX_NAME);

        if (esStats != null) {
            retVal = new ElasticMetricStatistics();
            retVal.setMaximum(roundFix(esStats.getValue(), 10));
        }

        return retVal;
    }
}
