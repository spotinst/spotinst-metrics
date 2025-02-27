//package com.spotinst.metrics.bl.parsers;
////
////import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
////import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
////import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
////import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
////import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
////import org.apache.commons.collections4.CollectionUtils;
//////import org.elasticsearch.search.aggregations.Aggregations;
////import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
//////import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
////import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
//////import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
////import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
//////import org.elasticsearch.search.aggregations.bucket.range.Range;
////import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////
////import java.util.HashMap;
////import java.util.LinkedList;
////import java.util.List;
////import java.util.Map;
////
////public class MetricRangeAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {
////
////    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRangeAggregationParser.class);
////
////    public MetricRangeAggregationParser(String aggregationName, ElasticMetricStatisticsRequest metricStatsRequest) {
////        super(aggregationName, metricStatsRequest);
////    }
////
////    @Override
////    public Map<AggCompositeKey, Aggregation> parse(Map<AggCompositeKey, Aggregation> aggsMapByKeys, Map<String, ElasticMetricAggregations> byRefResultMap) {
////        if(aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
////            String msg = "No aggregations to parse, skipping RANGE aggregation parser";
////            LOGGER.debug(msg);
////            return aggsMapByKeys;
////        }
////
////        // ******************************************************************************************************
////        //
////        // RANGE IS BREAKING THE GROUP-BY STRUCTURE - CREATES SEQUENTIAL GROUPS OF METRICS BASED ON RANGE VALUES
////        //
////        // ******************************************************************************************************
////
////        // New result map
////        Map<String, ElasticMetricAggregations> workingMap = deepCopy(byRefResultMap);
////        byRefResultMap.clear();
////
////        // Reference result object is using a linked hash map, I've added the order control for readability
////        Integer orderControl = 0;
////
////        // Next depth map
////        Map<AggCompositeKey, Aggregation> nextDepth = new HashMap<>();
////
////        for(AggCompositeKey compositeKey : aggsMapByKeys.keySet()) {
////            String       termKey = compositeKey.getTermKey();
////            Aggregation aggs    = aggsMapByKeys.get(compositeKey);
////
////            if (aggs instanceof RangeAggregation) {
////                RangeAggregation range = (RangeAggregation) aggs.get(aggregationName); //TODO Tal check the casting
////            //            ParsedRange parsedRange = aggs.get(aggregationName);
////
////            if (range == null) {
////                String msg = "Cannot get elastic search range buckets for [%s], skipping to the next aggregation level/sibling";
////                LOGGER.debug(String.format(msg, aggregationName));
////                continue;
////            }
////
////            LOGGER.debug(String.format("Starting to report response objects for RANGE aggregation value [%s]", aggregationName));
////
////            // This is the metric we're going to break into smaller sequential range groups for this iteration
////            ElasticMetricAggregations iterationMetricSequence;
////
////            if(workingMap.containsKey(termKey)) {
////                iterationMetricSequence = workingMap.get(termKey);
////            }
////            else {
////                iterationMetricSequence = new ElasticMetricAggregations();
////            }
////
//////            List<? extends Range.Bucket> buckets = parsedRange.getBuckets();
////            List<? extends RangeAggregation.Bucket> buckets = range.getBuckets();
////
////            if (CollectionUtils.isEmpty(buckets) == false) {
////                for (RangeAggregation.Bucket bucket : buckets) {
////                    String rangeValue = bucket.getKeyAsString();
////
////                    // Create the range dimension
////                    ElasticMetricDimension
////                            rangedDimension = new ElasticMetricDimension(metricStatisticsRequest.getMetricName(),
////                                                                    rangeValue);
////
////                    // Create a new metric aggregation based on the iteration metric sequence
////                    ElasticMetricAggregations    metricAgg          = new ElasticMetricAggregations();
////                    List<ElasticMetricDatapoint> deepCopyDatapoints = deepCopyDatapoints(iterationMetricSequence.getDatapoints());
////                    metricAgg.setDatapoints(deepCopyDatapoints);
////
////                    // Get iteration metric dimensions and deep copy its values with the new range dimension
////                    List<ElasticMetricDimension> iter    = iterationMetricSequence.getDimensions();
////                    List<ElasticMetricDimension> newDims = new LinkedList<>();
////
////                    if(CollectionUtils.isEmpty(iter) == false) {
////                        iter.forEach(d -> {
////                            ElasticMetricDimension newDim = new ElasticMetricDimension(d.getName(), d.getValue());
////                            newDims.add(newDim);
////                        });
////                    }
////
////                    // Append a new dimension with ranged values
////                    newDims.add(rangedDimension);
////                    metricAgg.setDimensions(newDims);
////
////                    // Create a complex key for deeper aggregation level identification
////                    //                    String complexKey = String.format("%s_%s_%s", orderControl, termKey, rangeValue);
////                    String complexKey = orderControl.toString();
////                    byRefResultMap.put(complexKey, metricAgg);
////
////                    // Prepare the depth of the inner aggregations
////                    Aggregation innerAggs = bucket.getAggregations();
////
////                    if (innerAggs != null) {
////                        AggCompositeKey newKey = new AggCompositeKey();
////                        newKey.setTermKey(complexKey);
////                        nextDepth.put(newKey, innerAggs);
////                    }
////
////                    ++orderControl;
////                }
////
////                LOGGER.debug(String.format("Finished creating response objects for GROUP BY aggregation value [%s]", aggregationName));
////            }
////        }
////
////        if(nextDepth.isEmpty() == false) {
////            LOGGER.debug(String.format("Proceeding to the next aggregation depth to parse [%s] inner aggregations...",
////                                       nextDepth.size()));
////        } else {
////            LOGGER.debug("Reached to the deepest aggregation level, completed.");
////        }
////        return nextDepth;
////    }
////}
//
////TODO Tal : Need to be very careful with the implementation here. Need to check the casting and the instance of check
//import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
//import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
//import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
//import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
//import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
//import org.apache.commons.collections4.CollectionUtils;
////import org.elasticsearch.search.aggregations.Aggregations;
//import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
////import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
//import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
////import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
//import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
////import org.elasticsearch.search.aggregations.bucket.range.Range;
//import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.spotinst.metrics.bl.parsers.BaseMetricAggregationParser;
//import org.apache.commons.collections4.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//public class MetricRangeAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRangeAggregationParser.class);
//
//    public MetricRangeAggregationParser(String aggregationName, ElasticMetricStatisticsRequest metricStatsRequest) {
//        super(aggregationName, metricStatsRequest);
//    }
//
//    @Override
//    public Map<AggCompositeKey, Aggregation> parse(Map<AggCompositeKey, Aggregation> aggsMapByKeys, Map<String, ElasticMetricAggregations> byRefResultMap) {
//        if (aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
//            String msg = "No aggregations to parse, skipping RANGE aggregation parser";
//            LOGGER.debug(msg);
//            return aggsMapByKeys;
//        }
//
//        // New result map
//        Map<String, ElasticMetricAggregations> workingMap = deepCopy(byRefResultMap);
//        byRefResultMap.clear();
//
//        Integer orderControl = 0;
//        Map<AggCompositeKey, Aggregation> nextDepth = new HashMap<>();
//
//        for (AggCompositeKey compositeKey : aggsMapByKeys.keySet()) {
//            String termKey = compositeKey.getTermKey();
//            Aggregation aggs = aggsMapByKeys.get(compositeKey);
//
//            if (aggs._kind() == Aggregation.Kind.Range) {
//                RangeAggregation range = aggs.range();
//
//                LOGGER.debug(String.format("Starting to report response objects for RANGE aggregation value [%s]", aggregationName));
//
//                ElasticMetricAggregations iterationMetricSequence;
//
//                if (workingMap.containsKey(termKey)) {
//                    iterationMetricSequence = workingMap.get(termKey);
//                } else {
//                    iterationMetricSequence = new ElasticMetricAggregations();
//                }
//
//                List<? extends RangeBucket> buckets = range.buckets().array();
//
//                if (!CollectionUtils.isEmpty(buckets)) {
//                    for (RangeBucket bucket : buckets) {
//                        String rangeValue = bucket.key().stringValue();
//
//                        ElasticMetricDimension rangedDimension = new ElasticMetricDimension(metricStatisticsRequest.getMetricName(), rangeValue);
//
//                        ElasticMetricAggregations metricAgg = new ElasticMetricAggregations();
//                        List<ElasticMetricDatapoint> deepCopyDatapoints = deepCopyDatapoints(iterationMetricSequence.getDatapoints());
//                        metricAgg.setDatapoints(deepCopyDatapoints);
//
//                        List<ElasticMetricDimension> iter = iterationMetricSequence.getDimensions();
//                        List<ElasticMetricDimension> newDims = new LinkedList<>();
//
//                        if (!CollectionUtils.isEmpty(iter)) {
//                            iter.forEach(d -> {
//                                ElasticMetricDimension newDim = new ElasticMetricDimension(d.getName(), d.getValue());
//                                newDims.add(newDim);
//                            });
//                        }
//
//                        newDims.add(rangedDimension);
//                        metricAgg.setDimensions(newDims);
//
//                        String complexKey = orderControl.toString();
//                        byRefResultMap.put(complexKey, metricAgg);
//
//                        Aggregation innerAggs = bucket.aggregations();
//
//                        if (innerAggs != null) {
//                            AggCompositeKey newKey = new AggCompositeKey();
//                            newKey.setTermKey(complexKey);
//                            nextDepth.put(newKey, innerAggs);
//                        }
//
//                        ++orderControl;
//                    }
//
//                    LOGGER.debug(String.format("Finished creating response objects for GROUP BY aggregation value [%s]", aggregationName));
//                }
//            } else {
//                String msg = "Cannot get elastic search range buckets for [%s], skipping to the next aggregation level/sibling";
//                LOGGER.debug(String.format(msg, aggregationName));
//            }
//        }
//
//        if (!nextDepth.isEmpty()) {
//            LOGGER.debug(String.format("Proceeding to the next aggregation depth to parse [%s] inner aggregations...", nextDepth.size()));
//        } else {
//            LOGGER.debug("Reached to the deepest aggregation level, completed.");
//        }
//        return nextDepth;
//    }
//}