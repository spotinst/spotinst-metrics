package com.spotinst.metrics.bl.parsers.stats;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.MaxAggregate;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.spotinst.metrics.bl.index.spotinst.RawIndexManager.roundFix;

public class MetricMaxParser implements IMetricStatParser {

    @Override
    public ElasticMetricStatistics parse(Map<String, Aggregate> aggregateMap) {
        ElasticMetricStatistics retVal = null;

        if (MapUtils.isNotEmpty(aggregateMap)) {

            MaxAggregate maxAggregate = aggregateMap.get(Aggregate.Kind.Max.name()).max();

            if (maxAggregate != null) {
                retVal = new ElasticMetricStatistics();
                retVal.setMaximum(roundFix(maxAggregate.value(), 10));
            }
        }

        return retVal;
    }
}
