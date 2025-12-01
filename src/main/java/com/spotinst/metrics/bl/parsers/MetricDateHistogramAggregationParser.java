package com.spotinst.metrics.bl.parsers;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MetricDateHistogramAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricDateHistogramAggregationParser.class);

    public MetricDateHistogramAggregationParser(String aggregationName,
                                                ElasticMetricStatisticsRequest metricStatsRequest) {
        super(aggregationName, metricStatsRequest);
    }

    @Override
    public Map<AggCompositeKey, Map<String, Aggregate>> parse(
            Map<AggCompositeKey, Map<String, Aggregate>> aggsMapByKeys,
            Map<String, ElasticMetricAggregations> byRefResultMap) {
        Map<AggCompositeKey, Map<String, Aggregate>> nextDepth = new HashMap<>();

        if (aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
            String msg = "No aggregations to parse, skipping DATE HISTOGRAM aggregation parser";
            LOGGER.debug(msg);
            return aggsMapByKeys;
        }

        for (AggCompositeKey compositeKey : aggsMapByKeys.keySet()) {
            Map<String, Aggregate> aggs       = aggsMapByKeys.get(compositeKey);
            String                 metricName = compositeKey.getTermKey();

            Aggregate aggregation = aggs.get(aggregationName);

            if (Objects.isNull(aggregation)) {
                String msg =
                        "Cannot get elastic search date histogram buckets for [%s], skipping to the next aggregation level/sibling";
                LOGGER.debug(String.format(msg, aggregationName));
                continue;
            }

            DateHistogramAggregate histogram = aggregation.dateHistogram();

            List<DateHistogramBucket> buckets = histogram.buckets().array();

            if (CollectionUtils.isNotEmpty(buckets)) {
                LOGGER.debug(String.format(
                        "Starting to create response objects for DATE HISTOGRAM aggregation for metric value [%s]",
                        metricName));

                List<ElasticMetricDatapoint> newDatapoints = new ArrayList<>();

                for (DateHistogramBucket bucket : buckets) {
                    String                    dateKey = bucket.keyAsString();
                    ElasticMetricAggregations esMetricAggregations;

                    // Add to the aggregation parsing result map
                    if (byRefResultMap.containsKey(metricName)) {
                        esMetricAggregations = byRefResultMap.get(metricName);
                    }
                    else {
                        esMetricAggregations = new ElasticMetricAggregations();
                    }

                    ElasticMetricDatapoint dpNew = new ElasticMetricDatapoint();
                    dpNew.setTimestamp(dateKey);
                    esMetricAggregations.addDatapoint(dpNew);
                    newDatapoints.add(dpNew);

                    // Prepare the depth of the inner aggregations
                    Map<String, Aggregate> innerAggs = bucket.aggregations();

                    if (Objects.nonNull(innerAggs)) {
                        AggCompositeKey newCompositeKey = new AggCompositeKey();
                        newCompositeKey.setTermKey(metricName);
                        newCompositeKey.setTimestampKey(dateKey);
                        nextDepth.put(newCompositeKey, innerAggs);
                    }
                }

                ElasticMetricAggregations preAdd;

                if (byRefResultMap.containsKey(metricName)) {
                    preAdd = byRefResultMap.get(metricName);
                    preAdd.setDatapoints(newDatapoints);
                }
                else {
                    preAdd = new ElasticMetricAggregations();
                    preAdd.setDatapoints(newDatapoints);
                    byRefResultMap.put(metricName, preAdd);
                }

                LOGGER.debug(String.format(
                        "Finished creating response objects for DATE HISTOGRAM aggregation for metric value [%s]",
                        metricName));
            }
        }

        if (MapUtils.isNotEmpty(nextDepth)) {
            LOGGER.debug(String.format("Proceeding to the next aggregation depth to parse [%s] inner aggregations...",
                                       nextDepth.size()));
        }
        else {
            LOGGER.debug("Reached to the deepest aggregation level, completed.");
        }

        return nextDepth;
    }
}
