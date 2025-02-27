package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager.roundFix;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_SUM_NAME;

public class MetricSumParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Aggregate aggregate) {
        ElasticMetricStatistics retVal = null;

        if (aggregate != null && aggregate._kind() == Aggregate.Kind.Sum) {
            SumAggregate sumAggregate   = aggregate.sum();

            if (sumAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setSum(roundFix(sumAggregate.value(), 10));
            }
        }
        return retVal;
    }
}
