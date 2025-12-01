package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.AvgAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.RawIndexManager.roundFix;

public class MetricAverageParser implements IMetricStatParser {
    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;


        if (MapUtils.isNotEmpty(aggregateMap)) {
            Aggregate aggregation = aggregateMap.get(Aggregate.Kind.Avg.name());

            if (aggregation != null) {
                AvgAggregate avgAggregate = aggregateMap.get(Aggregate.Kind.Avg.name()).avg();

                retVal = new ElasticMetricStatistics();
                retVal.setAverage(roundFix(avgAggregate.value(), 10));
            }
        }

        return retVal;
    }
}
