package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.RawIndexManager.roundFix;

public class MetricSumParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;

        if (MapUtils.isNotEmpty(aggregateMap)) {
            SumAggregate sumAggregate = aggregateMap.get(Aggregate.Kind.Sum.name()).sum();

            if (sumAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setSum(roundFix(sumAggregate.value(), 10));
            }
        }
        return retVal;
    }
}
