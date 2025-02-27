package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_MAX_NAME;

public class MetricMaxParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Aggregate aggregate) {
        ElasticMetricStatistics retVal  = null;

        if (aggregate != null && aggregate._kind() == Aggregate.Kind.Max) {
            MaxAggregate maxAggregate = aggregate.max();

            if (maxAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setAverage(roundFix(maxAggregate.value(), 10));
            }
        }

        return retVal;
    }
}
