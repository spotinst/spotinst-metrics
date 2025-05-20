package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.MinAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.BaseIndexManager2.roundFix;

public class MetricMinParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;

        if (MapUtils.isNotEmpty(aggregateMap)) {
            MinAggregate minAggregate = aggregateMap.get(Aggregate.Kind.Min.name()).min();

            if (minAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setMinimum(roundFix(minAggregate.value(), 10));
            }
        }

        return retVal;
    }
}