package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_AVERAGE_NAME;

public class MetricAverageParser implements IMetricStatParser {
    @Override
    public ElasticMetricStatistics parse(Aggregate aggregate) {
        ElasticMetricStatistics retVal  = null;

        if (aggregate != null && aggregate._kind() == Aggregate.Kind.Avg) {
            AvgAggregate avgAggregate = aggregate.avg();

            if (avgAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setAverage(roundFix(avgAggregate.value(), 10));
            }
        }

        return retVal;
    }
}
