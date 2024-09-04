package com.spotinst.metrics.bl.parsers;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMetricAggregationParser<T extends ElasticMetricStatisticsRequest>
        implements IMetricAggregationParser {
    protected String aggregationName;
    protected T      metricStatisticsRequest;

    protected BaseMetricAggregationParser(String aggregationName, T metricStatisticsRequest) {
        this.aggregationName = aggregationName;
        this.metricStatisticsRequest = metricStatisticsRequest;
    }

    public String getAggregationName() {
        return aggregationName;
    }

    protected Map<String, ElasticMetricAggregations> deepCopy(Map<String, ElasticMetricAggregations> byRefResultMap) {
        // No need for linked hash map usage, since the cloned map is redundant after each parser action
        Map<String, ElasticMetricAggregations> retVal = new HashMap<>();

        if(byRefResultMap != null && byRefResultMap.isEmpty() == false) {
            byRefResultMap.keySet().forEach(k -> {
                ElasticMetricAggregations    metAgg        = byRefResultMap.get(k);

                // Deep copy inner objects
                List<ElasticMetricDimension> newDimensions = deepCopyDimensions(metAgg.getDimensions());
                List<ElasticMetricDatapoint> newDatapoints = deepCopyDatapoints(metAgg.getDatapoints());

                ElasticMetricAggregations newMetAgg = new ElasticMetricAggregations();
                newMetAgg.setDimensions(newDimensions);
                newMetAgg.setDatapoints(newDatapoints);

                retVal.put(k, newMetAgg);
            });
        }

        return retVal;
    }

    protected List<ElasticMetricDimension> deepCopyDimensions(List<ElasticMetricDimension> dimensions) {
        List<ElasticMetricDimension> retVal = new ArrayList<>();

        if(CollectionUtils.isEmpty(dimensions) == false) {
            dimensions.forEach(d -> {
                ElasticMetricDimension newItem = new ElasticMetricDimension(d.getName(), d.getValue());
                retVal.add(newItem);
            });
        }

        return retVal;
    }

    protected List<ElasticMetricDatapoint> deepCopyDatapoints(List<ElasticMetricDatapoint> datapoints) {
        List<ElasticMetricDatapoint> retVal = new ArrayList<>();

        if(CollectionUtils.isEmpty(datapoints) == false) {
            datapoints.forEach(d -> {
                ElasticMetricStatistics newStats  = new ElasticMetricStatistics();
                ElasticMetricStatistics prevStats = d.getStatistics();

                if(prevStats != null) {
                    newStats.setCount(prevStats.getCount());
                    newStats.setSum(prevStats.getSum());
                    newStats.setMinimum(prevStats.getMinimum());
                    newStats.setMaximum(prevStats.getMaximum());
                    newStats.setAverage(prevStats.getAverage());
                }

                ElasticMetricDatapoint newItem = new ElasticMetricDatapoint(newStats);
                newItem.setTimestamp(d.getTimestamp());
                retVal.add(newItem);
            });
        }

        return retVal;
    }
}
