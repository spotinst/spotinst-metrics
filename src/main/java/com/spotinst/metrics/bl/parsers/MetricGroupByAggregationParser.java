package com.spotinst.metrics.bl.parsers;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricGroupByAggregationParser extends BaseMetricAggregationParser<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatsAggregationParser.class);

    public MetricGroupByAggregationParser(String aggregationName, ElasticMetricStatisticsRequest metricStatsRequest) {
        super(aggregationName, metricStatsRequest);
    }

    @Override
    public Map<AggCompositeKey, Aggregations> parse(Map<AggCompositeKey, Aggregations> aggsMapByKeys,
                                                    Map<String, ElasticMetricAggregations> byRefResultMap) {
        if (aggsMapByKeys == null || aggsMapByKeys.isEmpty()) {
            String msg = "No aggregations to parse, skipping GROUP BY aggregation parser";
            LOGGER.debug(msg);
            return aggsMapByKeys;
        }

        // ***********************************************************************************************
        //
        // GROUP BY IS THE HIGHEST AGGREGATION LEVEL, IT MIGHT BE CHAINED WITH OTHER GROUP BY AGGREGATIONS
        // GROUP BY IS BREAKING THE RESULT STRUCTURE
        //
        // ************************************************************************************************

        // New result map
        Map<String, ElasticMetricAggregations> workingMap = deepCopy(byRefResultMap);
        byRefResultMap.clear();

        // Next depth map
        Map<AggCompositeKey, Aggregations> nextDepth = new HashMap<>();

        for (AggCompositeKey key : aggsMapByKeys.keySet()) {
            String       termKey = key.getTermKey();
            Aggregations aggs    = aggsMapByKeys.get(key);
            Terms        terms   = aggs.get(aggregationName);

            if (terms == null) {
                String msg =
                        "Cannot get elastic search term buckets for [%s], skipping to the next aggregation level/sibling";
                LOGGER.debug(String.format(msg, aggregationName));
                continue;
            }

            List<String> groupByValues = metricStatisticsRequest.getGroupBy();
            String       groupByValue  = "";

            // Group by values should not be empty if we've registered the GroupBy aggregator parser
            if (CollectionUtils.isEmpty(groupByValues) == false) {
                for (String value : groupByValues) {
                    if (aggregationName.toLowerCase().contains(value.toLowerCase())) {
                        groupByValue = value;
                        break;
                    }
                }
            }
            LOGGER.debug(String.format("Starting to report response objects for GROUP BY aggregation value [%s]",
                                       groupByValue));

            List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();

            if (CollectionUtils.isEmpty(buckets) == false) {
                for (Terms.Bucket bucket : buckets) {
                    String                 valueKey  = bucket.getKey().toString();
                    ElasticMetricDimension dimension = new ElasticMetricDimension(groupByValue, valueKey);
                    String                 identifierKey;

                    // Add to the result map
                    ElasticMetricAggregations aggToAppend;
                    if (workingMap.containsKey(termKey)) {
                        // When the group-by is part of multiple group-by chain aggregations - use the previous group-by key to fetch items from result map
                        aggToAppend = new ElasticMetricAggregations();
                        ElasticMetricAggregations    toCopy           = workingMap.get(termKey);
                        List<ElasticMetricDimension> copiedDimensions = deepCopyDimensions(toCopy.getDimensions());
                        copiedDimensions.add(dimension);
                        List<ElasticMetricDatapoint> copiedDatapoints = deepCopyDatapoints(toCopy.getDatapoints());
                        aggToAppend.setDimensions(copiedDimensions);
                        aggToAppend.setDatapoints(copiedDatapoints);

                        identifierKey = String.format("%s_%s", termKey, valueKey);
                        byRefResultMap.put(identifierKey, aggToAppend);
                    }
                    else {
                        // When the group-by is the outer aggregation
                        aggToAppend = new ElasticMetricAggregations();
                        aggToAppend.addDimension(dimension);
                        byRefResultMap.put(valueKey, aggToAppend);
                        identifierKey = valueKey;
                    }

                    // Prepare the depth of the inner aggregations
                    Aggregations innerAggs = bucket.getAggregations();

                    if (innerAggs != null) {
                        AggCompositeKey newKey = new AggCompositeKey();
                        newKey.setTermKey(identifierKey);
                        nextDepth.put(newKey, innerAggs);
                    }
                }

                LOGGER.debug(String.format("Finished creating response objects for GROUP BY aggregation value [%s]",
                                           groupByValue));
            }
        }

        if (nextDepth.isEmpty() == false) {
            LOGGER.debug(String.format("Proceeding to the next aggregation depth to parse [%s] inner aggregations...",
                                       nextDepth.size()));
        }
        else {
            LOGGER.debug("Reached to the deepest aggregation level, completed.");
        }
        return nextDepth;
    }
}
