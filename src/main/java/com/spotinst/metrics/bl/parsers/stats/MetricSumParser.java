package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.InternalSum;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_SUM_NAME;

public class MetricSumParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Aggregations aggregation) {
        ElasticMetricStatistics retVal  = null;
        InternalSum        esStats = aggregation.get(AGG_METRIC_SUM_NAME);

        if (esStats != null) {
            retVal = new ElasticMetricStatistics();
            retVal.setSum(roundFix(esStats.getValue(), 10));
        }

        return retVal;
    }
}
