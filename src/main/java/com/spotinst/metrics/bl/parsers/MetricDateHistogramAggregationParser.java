package com.spotinst.metrics.bl.parsers;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MetricDateHistogramAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricDateHistogramAggregationParser.class);

    public MetricDateHistogramAggregationParser(String aggregationName, ElasticMetricStatisticsRequest metricStatsRequest) {
        super(aggregationName, metricStatsRequest);
    }

    @Override
    public Map<AggCompositeKey, Aggregate> parse(Map<AggCompositeKey, Aggregate> aggsMapByKeys, Map<String, ElasticMetricAggregations> byRefResultMap) {
                Map<AggCompositeKey, Aggregate> nextDepth = new HashMap<>();
                return nextDepth;
    }
//        if(aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
//            String msg = "No aggregations to parse, skipping DATE HISTOGRAM aggregation parser";
//            LOGGER.debug(msg);
//            return aggsMapByKeys;
//        }
//
//        // Next depth map
//        Map<AggCompositeKey, Aggregation> nextDepth = new HashMap<>();
//
//        for(AggCompositeKey compositeKey : aggsMapByKeys.keySet()) {
//            Aggregation aggs = aggsMapByKeys.get(compositeKey);
//            String metricName = compositeKey.getTermKey();
//
//            //TODO Tal: Fix the below
////            ParsedDateHistogram histogram = aggs.get(aggregationName);
////            Aggregate histogram = aggs.get(aggregationName);
//            Aggregate aggregate = aggs._get(aggregationName);
//
//            //            if (histogram == null) {
//            if (histogram == null || !histogram._isVariant(AggregateVariant.DateHistogram)) {
//                String msg =
//                        "Cannot get elastic search date histogram buckets for [%s], skipping to the next aggregation level/sibling";
//                LOGGER.debug(String.format(msg, aggregationName));
//                continue;
//            }
//
//            List<? extends Histogram.Bucket> buckets = histogram.getBuckets();
//
//            if (CollectionUtils.isEmpty(buckets) == false) {
//                LOGGER.debug(String.format("Starting to report response objects for DATE HISTOGRAM aggregation for metric value [%s]", metricName));
//
//                List<ElasticMetricDatapoint> newDatapoints = new ArrayList<>();
//                for (Histogram.Bucket bucket : buckets) {
//                    String               dateKey   = bucket.getKeyAsString();
//                    ElasticMetricAggregations elasticMetricAggregations;
//
//                    // Add to the aggregation parsing result map
//                    if(byRefResultMap.containsKey(metricName)) {
//                        elasticMetricAggregations = byRefResultMap.get(metricName);
//                    } else {
//                        elasticMetricAggregations = new ElasticMetricAggregations();
//                    }
//
//                    ElasticMetricDatapoint dpNew = new ElasticMetricDatapoint();
//                    dpNew.setTimestamp(dateKey);
//                    elasticMetricAggregations.addDatapoint(dpNew);
//                    newDatapoints.add(dpNew);
//
//                    // Prepare the depth of the inner aggregations
//                    Aggregation innerAggs = bucket.getAggregations();
//
//                    if (innerAggs != null) {
//                        AggCompositeKey newCompositeKey = new AggCompositeKey();
//                        newCompositeKey.setTermKey(metricName);
//                        newCompositeKey.setTimestampKey(dateKey);
//                        nextDepth.put(newCompositeKey, innerAggs);
//                    }
//                }
//
//                ElasticMetricAggregations preAdd;
//
//                if(byRefResultMap.containsKey(metricName)) {
//                    preAdd = byRefResultMap.get(metricName);
//                    preAdd.setDatapoints(newDatapoints);
//                } else {
//                    preAdd = new ElasticMetricAggregations();
//                    preAdd.setDatapoints(newDatapoints);
//                    byRefResultMap.put(metricName, preAdd);
//                }
//
//                LOGGER.debug(String.format("Finished creating response objects for DATE HISTOGRAM aggregation for metric value [%s]", metricName));
//            }
//        }
//
//        if(nextDepth.isEmpty() == false) {
//            LOGGER.debug(String.format("Proceeding to the next aggregation depth to parse [%s] inner aggregations...",
//                                       nextDepth.size()));
//        } else {
//            LOGGER.debug("Reached to the deepest aggregation level, completed.");
//        }
//        return nextDepth;
//    }
}
