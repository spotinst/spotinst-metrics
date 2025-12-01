package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.ValueCountAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.RawIndexManager.roundFix;

public class MetricCountParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;

        if (MapUtils.isNotEmpty(aggregateMap)) {
            Aggregate aggregation = aggregateMap.get(Aggregate.Kind.ValueCount.name());

            if (aggregation != null) {
                ValueCountAggregate valueCount = aggregateMap.get(Aggregate.Kind.ValueCount.name()).valueCount();
                retVal = new ElasticMetricStatistics();
                retVal.setCount(roundFix(valueCount.value(), 10));
            }
        }

        return retVal;
    }
}
