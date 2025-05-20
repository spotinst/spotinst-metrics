package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.ValueCountAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager2.roundFix;

public class MetricCountParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;

        if (MapUtils.isNotEmpty(aggregateMap)) {
            ValueCountAggregate valueCount = aggregateMap.get(Aggregate.Kind.ValueCount.name()).valueCount();

            if (valueCount != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setCount(roundFix(valueCount.value(), 10));
            }
        }

        return retVal;
    }
}
