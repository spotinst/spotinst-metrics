package com.spotinst.metrics.bl.parsers;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.spotinst.metrics.bl.parsers.stats.IMetricStatParser;
import com.spotinst.metrics.bl.parsers.stats.MetricStatsParserFactory;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.apache.commons.collections4.CollectionUtils;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricStatsAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatsAggregationParser.class);

    public MetricStatsAggregationParser(String aggregationName, ElasticMetricStatisticsRequest metricStatsRequest) {
        super(aggregationName, metricStatsRequest);
    }

    @Override
    public Map<AggCompositeKey, Aggregate> parse(Map<AggCompositeKey, Aggregate> aggsMapByKeys,
                                                 Map<String, ElasticMetricAggregations> byRefResultMap) {
        if (aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
            String msg = "No aggregations to parse, skipping METRIC STATS aggregation parser";
            LOGGER.debug(msg);
            return aggsMapByKeys;
        }

        MetricStatisticEnum statistic  = metricStatisticsRequest.getStatistic();
        IMetricStatParser   statParser = MetricStatsParserFactory.getParser(statistic);

        // <MetricKey, <TimeStamp, DataPoint>>
        Map<String, Map<String, ElasticMetricDatapoint>> dataPointsByTimeStampByMetricKey =
                createMetricKeyToDataPointByTimestampMap(byRefResultMap);

        // Next depth map
        Map<AggCompositeKey, Aggregate> nextDepth = new HashMap<>();

        for (AggCompositeKey compositeKey : aggsMapByKeys.keySet()) {
            String       metricKey    = compositeKey.getTermKey();
            String       timestampKey = compositeKey.getTimestampKey();
            Aggregate aggregate = aggsMapByKeys.get(compositeKey);

            // Create metric statistics from elastic search response
            ElasticMetricStatistics statistics = statParser.parse(aggregate);

            if (statistics == null) {
                String msg = "Cannot get elastic search stats [%s] for time [%s], skipping";
                LOGGER.debug(String.format(msg, aggregationName, timestampKey));
                continue;
            }

            Map<String, ElasticMetricDatapoint> byTimestamp = dataPointsByTimeStampByMetricKey.get(metricKey);
            if (byTimestamp != null && byTimestamp.isEmpty() == false) {

                // When a time interval is used, we need to glue each data point to its relevant time
                ElasticMetricDatapoint dp = byTimestamp.get(timestampKey);

                if (dp == null) {
                    dp = new ElasticMetricDatapoint();
                }
                dp.setTimestamp(timestampKey);
                dp.setStatistics(statistics);

            }
            else {

                // In this case dimension is without timestamp since no time interval was used, just add as is
                ElasticMetricDatapoint dp = new ElasticMetricDatapoint();
                dp.setStatistics(statistics);

                ElasticMetricAggregations esMetricAggregations;
                if (byRefResultMap.containsKey(metricKey)) {
                    esMetricAggregations = byRefResultMap.get(metricKey);
                    esMetricAggregations.addDatapoint(dp);
                }
                else {
                    // Should happen if the stats aggregation parser is the only parser,
                    // meaning it is the highest in the parsers stack
                    esMetricAggregations = new ElasticMetricAggregations();
                    esMetricAggregations.addDatapoint(dp);
                    byRefResultMap.put(metricKey, esMetricAggregations);
                }
            }
        }

        LOGGER.debug(
                String.format("Finished creating response objects for METRIC STATS aggregation [%s]", aggregationName));
        LOGGER.debug("Reached to the deepest aggregation level, completed.");

        // Metric stats should be the leaf of the aggregation tree, returns an empty depth map
        return nextDepth;
    }

    private Map<String, Map<String, ElasticMetricDatapoint>> createMetricKeyToDataPointByTimestampMap(
            Map<String, ElasticMetricAggregations> byRefResultMap) {
        Map<String, Map<String, ElasticMetricDatapoint>> retVal = new HashMap<>();

        for (String byRefKey : byRefResultMap.keySet()) {
            ElasticMetricAggregations metAgg = byRefResultMap.get(byRefKey);
            if (metAgg == null) {
                continue;
            }

            List<ElasticMetricDatapoint> datapoints = metAgg.getDatapoints();
            if (CollectionUtils.isEmpty(datapoints)) {
                continue;
            }

            Map<String, ElasticMetricDatapoint> dpByTimeStamp = new HashMap<>();
            datapoints.forEach(dp -> dpByTimeStamp.put(dp.getTimestamp(), dp));
            retVal.put(byRefKey, dpByTimeStamp);
        }

        return retVal;
    }
}