package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.parsers.BaseMetricAggregationParser;
import com.spotinst.metrics.bl.parsers.IMetricAggregationParser;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.dal.models.elastic.*;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.AbstractPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_COUNT_FIELD_PATH_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_KEYWORD_SUFFIX;
import static com.spotinst.metrics.commons.constants.MetricsConstants.MetricScripts.AVG_BUCKET_SCRIPT_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.MetricScripts.EMPTY_COUNT_BUCKET_SCRIPT_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Namespace.*;

public abstract class BaseIndexManager<T extends ElasticMetricStatisticsRequest>
        implements IFilterBuilder, IAggregationBuilder, ISearchResponseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseIndexManager.class);

    protected T                              request;
    protected List<IMetricAggregationParser> parsers;

    public BaseIndexManager(T request) {
        this.request = request;
        this.parsers = new ArrayList<>();
    }

    // region Filters
    /**
     * Analytics request to be parsed and built onto the elastic search request builder
     * The query which is being built is optimized for search performance - do not change ordering
     *
     * @param sourceBuilder elastic search request source builder
     * @throws DalException
     */
    @Override
    public void setFilters(SearchSourceBuilder sourceBuilder) throws DalException {
        TermsQueryBuilder       namespaceQuery     = createNamespaceQuery();
        List<TermsQueryBuilder> termsQueries       = createDimensionsQueries();
        ExistsQueryBuilder      existsQuery        = createMetricExistsQuery();
        RangeQueryBuilder       timeQuery          = createTimeQuery();
        BoolQueryBuilder        dataOwnershipQuery = createDataOwnershipQuery();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.must(existsQuery);

        // Use filter instead of query to benefit from elastic search cache and avoid document scoring
        boolQuery.filter(namespaceQuery);

        // Filters with different keys should have the AND operator between them.
        // Example: balancer_id, target_set_id & target_id should be used with an AND operator
        for(TermsQueryBuilder tQuery : termsQueries) {
            boolQuery.filter(tQuery);
        }

        // Append account id on filter to enforce data ownership
        if(dataOwnershipQuery != null) {
            boolQuery = boolQuery.filter(dataOwnershipQuery);
        }

        boolQuery.filter(timeQuery);
        sourceBuilder.query(boolQuery);

        // Do not return documents, we care about the aggregation content
        sourceBuilder.size(0);
    }

    protected abstract BoolQueryBuilder createDataOwnershipQuery();

    private List<TermsQueryBuilder> createDimensionsQueries() throws DalException {
        List<TermsQueryBuilder>      retVal;
        List<ElasticMetricDimension> dimensions = request.getDimensions();

        if(CollectionUtils.isEmpty(dimensions) == false) {
            retVal = new ArrayList<>();

            LOGGER.debug("Starting to report match query filter...");
            // Filters with the same key should have the OR operator between them.
            // Example: multiple dimensions with different values e.g target_id with id-123 and id-234
            Map<String, List<Object>> sameDimensionQueries = new HashMap<>();

            dimensions.forEach(d -> {
                String path           = getDimensionQueryFieldPath(d.getName());
                String dimensionValue = d.getValue();

                // If we've already have a query with the same path - append, else add
                if(sameDimensionQueries.containsKey(path)) {
                    List<Object> toAppend = sameDimensionQueries.get(path);
                    toAppend.add(dimensionValue);
                } else {
                    List<Object> toAdd = new ArrayList<>();
                    toAdd.add(dimensionValue);
                    sameDimensionQueries.put(path, toAdd);
                }
            });

            for(String dimensionKey : sameDimensionQueries.keySet()) {
                List<Object> sameDimensionValues = sameDimensionQueries.get(dimensionKey);
                TermsQueryBuilder terms = new TermsQueryBuilder(dimensionKey, sameDimensionValues);
                retVal.add(terms);
            }

            LOGGER.debug(String.format("Created terms queries for [%s] dimensions", sameDimensionQueries.size()));
        } else {
            String errMsg = "Missing dimensions to report terms queries, must have at least one dimension. Cannot report filter";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        return retVal;
    }

    private TermsQueryBuilder createNamespaceQuery() throws DalException {
        TermsQueryBuilder retVal;
        Set<String>       namespaces   = new HashSet<>();
        String            reqNamespace = request.getNamespace();

        if(StringUtils.isEmpty(reqNamespace)) {
            String errMsg = "[namespace] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        } else {
            namespaces.add(reqNamespace);
            //TODO Tal : I think we can delete it
//            if(reqNamespace.toLowerCase().equals(BALANCER_NAMESPACE_OLD)) {
//                namespaces.add(BALANCER_NAMESPACE_NEW);
//            }
//            else if(reqNamespace.toLowerCase().equals(SYSTEM_NAMESPACE_OLD)) {
//                namespaces.add(SYSTEM_NAMESPACE_NEW);
//            }
        }

//        retVal = new TermsQueryBuilder(NAMESPACE_FIELD_PATH + METRIC_KEYWORD_SUFFIX, namespaces);
        retVal = new TermsQueryBuilder(NAMESPACE_FIELD_PATH, namespaces);
        return retVal;
    }

    private ExistsQueryBuilder createMetricExistsQuery() throws DalException {
        ExistsQueryBuilder metricExistQuery;
        String             metricName = request.getMetricName();

        if(StringUtils.isEmpty(metricName)) {
            String errMsg = "[metricName] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        LOGGER.debug(String.format("Created exists query on metric [%s]", metricName));
        String path = getMetricExistsFieldPath();
        metricExistQuery = QueryBuilders.existsQuery(path);

        return metricExistQuery;
    }

    private RangeQueryBuilder createTimeQuery() throws DalException {
        RangeQueryBuilder      rangeQuery = QueryBuilders.rangeQuery(TIMESTAMP_FIELD_PATH);
        ElasticMetricDateRange dateRange  = request.getDateRange();

        if(dateRange != null && dateRange.getFrom() != null && dateRange.getTo() != null) {
            LOGGER.debug("Starting to report query based on date range...");

            Date fromDate = dateRange.getFrom();
            Date toDate   = dateRange.getTo();

            String fromDateFormatted = ElasticMetricTimeUtils.dateToString(fromDate);
            String toDateFormatted   = ElasticMetricTimeUtils.dateToString(toDate);
            rangeQuery.from(fromDateFormatted);
            rangeQuery.to(toDateFormatted);
            LOGGER.debug(String.format("Finished creating query based on date range, from [%s] to [%s]",
                                       fromDate, toDate));
        } else {
            LOGGER.debug("Cannot report query, missing [dateRange] values");
        }

        return rangeQuery;
    }
    // endregion

    // region Statistics
    protected abstract AbstractAggregationBuilder createSumStatAggregation();

    private AbstractPipelineAggregationBuilder createAverageStatAggregation() {
        Map<String, String> bucketsScriptPathsMap = new HashMap<>();
        bucketsScriptPathsMap.put(AGG_PIPELINE_SUM_VALUE, AGG_METRIC_SUM_NAME);
        bucketsScriptPathsMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);

        String avgScriptStr = String.format(AVG_BUCKET_SCRIPT_FORMAT,
                                            AGG_PIPELINE_SUM_VALUE,
                                            AGG_PIPELINE_COUNT_VALUE);

        Script avgScript = new Script(avgScriptStr);

        BucketScriptPipelineAggregationBuilder bucketScript = PipelineAggregatorBuilders
                .bucketScript(AGG_METRIC_AVERAGE_NAME, bucketsScriptPathsMap, avgScript);

        return bucketScript;
    }

    private AbstractAggregationBuilder createMinStatAggregation() {
        String                fieldPath = getMetricQueryFieldPath();
        MinAggregationBuilder retVal    = AggregationBuilders.min(AGG_METRIC_MIN_NAME).field(fieldPath);
        return retVal;
    }

    private AbstractAggregationBuilder createMaxStatAggregation() {
        String                fieldPath = getMetricQueryFieldPath();
        MaxAggregationBuilder retVal    = AggregationBuilders.max(AGG_METRIC_MAX_NAME).field(fieldPath);
        return retVal;
    }

    private AbstractAggregationBuilder createCountStatAggregation() {
        String metricName = request.getMetricName();
        String fieldPath  = String.format(METRIC_COUNT_FIELD_PATH_FORMAT, metricName);

        SumAggregationBuilder retVal = AggregationBuilders.sum(AGG_METRIC_COUNT_NAME).field(fieldPath);
        return retVal;
    }

    private AbstractPipelineAggregationBuilder createBucketsFilterByCondition() {
        Map<String, String> bucketsPathsMap = new HashMap<>();
        bucketsPathsMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);

        String emptyBucketsScriptStr = String.format(EMPTY_COUNT_BUCKET_SCRIPT_FORMAT,
                                                     AGG_PIPELINE_COUNT_VALUE);

        Script emptyBucketsScript = new Script(emptyBucketsScriptStr);

        BucketSelectorPipelineAggregationBuilder bucketSelector = PipelineAggregatorBuilders
                .bucketSelector(EMPTY_COUNT_BUCKET_FILTER_NAME, bucketsPathsMap, emptyBucketsScript);

        return bucketSelector;
    }
    // endregion

    // region Aggregations
    @Override
    public void setAggregations(SearchSourceBuilder sourceBuilder)
            throws DalException, IOException {

        // Create range aggregation
        RangeAggregationBuilder rangeAggregation = createRangeAggregationQuery();

        // Create terms aggregations by 'group by' values
        ElasticMetricGroupByAggregations groupByAggs = createGroupByAggregation();

        // Create date histogram aggregation by time interval & date range
        DateHistogramAggregationBuilder timeIntervalAggregation = createTimeIntervalAggregation();

        setupAggregationsOrdering(rangeAggregation,
                                  timeIntervalAggregation,
                                  groupByAggs,
                                  sourceBuilder);
    }

    protected abstract String getMetricQueryFieldPath();

    protected abstract String getMetricExistsFieldPath();

    protected abstract String getDimensionQueryFieldPath(String dimension);

    private AbstractAggregationBuilder addStatisticsSubAggregations(AbstractAggregationBuilder parentAggregation) {
        MetricStatisticEnum statistic   = request.getStatistic();
        Boolean             isLegalStat = true;
        String              statAggName = "";

        switch (statistic) {
            case MINIMUM: {
                AbstractAggregationBuilder minStat = createMinStatAggregation();
                parentAggregation.subAggregation(minStat);
                statAggName = AGG_METRIC_MIN_NAME;
            }
            break;
            case MAXIMUM: {
                AbstractAggregationBuilder maxStat = createMaxStatAggregation();
                parentAggregation.subAggregation(maxStat);
                statAggName = AGG_METRIC_MAX_NAME;
            }
            break;
            case SUM: {
                AbstractAggregationBuilder sumStat = createSumStatAggregation();
                parentAggregation.subAggregation(sumStat);
                statAggName = AGG_METRIC_SUM_NAME;
            }
            break;
            case COUNT: {
                AbstractAggregationBuilder countStat = createCountStatAggregation();
                parentAggregation.subAggregation(countStat);
                statAggName = AGG_METRIC_COUNT_NAME;
            }
            break;
            case AVERAGE: {
                AbstractAggregationBuilder         sumStat   = createSumStatAggregation();
                AbstractAggregationBuilder         countStat = createCountStatAggregation();
                AbstractPipelineAggregationBuilder avgStat   = createAverageStatAggregation();

                parentAggregation.subAggregation(sumStat);
                parentAggregation.subAggregation(countStat);
                parentAggregation.subAggregation(avgStat);
                statAggName = AGG_METRIC_AVERAGE_NAME;
            }
            break;
            default: {
                isLegalStat = false;
                String format = "Statistics [%s] is not supported, cannot report aggregation based on statistic.";
                String errMsg = String.format(format, statistic);
                LOGGER.error(errMsg);
            }
            break;
        }

        if(isLegalStat) {
            parsers.add(0, getStatsAggregationParser(statAggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
        }

        return parentAggregation;
    }

    private AbstractAggregationBuilder createStatisticsAggregations() throws DalException {
        AbstractAggregationBuilder retVal      = null;
        MetricStatisticEnum        statistic   = request.getStatistic();
        Boolean                    isLegalStat = true;
        String                     statAggName = "";
        String                     errMsg      = "";

        switch (statistic) {
            case MINIMUM: {
                retVal = createMinStatAggregation();
            }
            break;
            case MAXIMUM: {
                retVal = createMaxStatAggregation();
            }
            break;
            case SUM: {
                retVal = createSumStatAggregation();
            }
            break;
            case COUNT: {
                retVal = createCountStatAggregation();
            }
            break;
            case AVERAGE: {
                isLegalStat = false;
                //                AbstractAggregationBuilder         sumStat   = createSumStatAggregation();
                //                AbstractAggregationBuilder         countStat = createCountStatAggregation();
                //                AbstractPipelineAggregationBuilder avgStat   = createAverageStatAggregation();

                // TODO: need to complete the way we're serving statistics on requests without parent aggregation

                statAggName = AGG_METRIC_AVERAGE_NAME;

                errMsg = "Cannot query for [average] statistic without timeInterval or range fields.";
            }
            break;
            default: {
                isLegalStat = false;
                String format = "Statistics [%s] is not supported, cannot report aggregation based on statistic.";
                errMsg = String.format(format, statistic);
                LOGGER.error(errMsg);
            }
            break;
        }

        if(isLegalStat) {
            parsers.add(0, getStatsAggregationParser(statAggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
        } else {
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        return retVal;
    }

    private DateHistogramAggregationBuilder createTimeIntervalAggregation() {
        DateHistogramAggregationBuilder dateAgg      = null;
        MetricsTimeIntervalEnum         timeInterval = request.getTimeInterval();

        if(timeInterval != null) {
            String timeIntervalStr = timeInterval.getName();
            LOGGER.debug(String.format("Starting to report time interval aggregation of [%s]...", timeInterval));

            // Add date histogram aggregation
            dateAgg = new DateHistogramAggregationBuilder(AGG_DATE_HISTOGRAM_NAME);
            dateAgg.field(TIMESTAMP_FIELD_PATH);

            // Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
            dateAgg.minDocCount(1);

            // Get the minute of the dateRange 'from' value and add it as a positive offset to the date histogram agg
            // Histogram returns the correct results but each key represent a round up value
            // Example of 1 HOUR and 3 PERIODS starting from 13:15 -
            //   - Without offset we'll receive histograms of 11:00, 12:00, 13:00.
            //     The 13:00 histogram include the values between 13:00-13:15.
            //   - We add an offset in order to change the histogram keys to show the exact time requested i.g 11:15, 12:15, 13:15

            ElasticMetricDateRange dateRange    = request.getDateRange();
            Date              fromDate     = dateRange.getFrom();
            int               minuteOfHour = ElasticMetricTimeUtils.getMinuteOfDate(fromDate);
            String            offset       = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, minuteOfHour);

            DateHistogramInterval dhInterval = new DateHistogramInterval(timeIntervalStr);
            dateAgg.offset(offset);
            dateAgg.dateHistogramInterval(dhInterval);

            LOGGER.debug(String.format("Finished creating time interval aggregation of [%s]", timeInterval));
        } else {
            LOGGER.debug("No time interval values for metrics aggregation, skipping time interval aggregation.");
        }

        return dateAgg;
    }

    private ElasticMetricGroupByAggregations createGroupByAggregation() throws IOException {

        ElasticMetricGroupByAggregations retVal = null;
        List<String> groupByDimensions = request.getGroupBy();

        if(CollectionUtils.isEmpty(groupByDimensions) == false) {
            LOGGER.debug("Starting to report [groupBy] aggregation...");

            List<TermsAggregationBuilder> groupByAggsOrder = new ArrayList<>();
            TermsAggregationBuilder       lastAgg          = null;

            for(String groupByDimension : groupByDimensions) {
                String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
                String aggName       = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);

                TermsAggregationBuilder iterationAgg = new TermsAggregationBuilder(
                        aggName.toUpperCase());

                // Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
                iterationAgg.minDocCount(1);

                iterationAgg.field(dimensionPath);

                if(lastAgg != null) {
                    lastAgg.subAggregation(iterationAgg);
                }

                lastAgg = iterationAgg;

                // Store the group-by aggregation order for deciding the parsers order later on
                groupByAggsOrder.add(iterationAgg);
            }

            retVal = new ElasticMetricGroupByAggregations(groupByAggsOrder);
            LOGGER.debug("Finished creating group by aggregation");
        } else {
            LOGGER.debug("No group by values for metrics aggregation, skipping group by aggregation.");
        }

        return retVal;
    }

    private RangeAggregationBuilder createRangeAggregationQuery() throws DalException {

        RangeAggregationBuilder retVal =  null;
        ElasticMetricRange      range  = request.getRange();

        if(range != null && range.hasPartialData() == false) {
            LOGGER.debug("Starting to report range aggregation...");

            String metricQueryFieldPath = getMetricQueryFieldPath();

            // Create range aggregation
            retVal = new RangeAggregationBuilder(AGG_RANGE_NAME);
            retVal.field(metricQueryFieldPath);

            Double minimum = range.getMinimum();
            Double maximum = range.getMaximum();
            Double gap     = range.getGap();

            if(gap == 0 || (minimum == 0 && maximum == 0)) {
                String errMsg = "Cannot report range aggregation when min;max;gap values are 0";
                LOGGER.error(errMsg);
                throw new DalException(errMsg);
            } else {
                Double from = roundFix(minimum, 10);
                Double to;
                for (Double i = from; i <= maximum; ) {
                    to = roundFix(i + gap, 10);
                    retVal.addRange(i, to);
                    i = to;
                }
                String msgFormat = "Finished creating range aggregation for value [%s]: minimum [%s] maximum [%s] with a gap of [%s]";
                LOGGER.debug(String.format(msgFormat, metricQueryFieldPath, minimum, maximum, gap));
            }
        } else {
            LOGGER.debug("No range values for metrics aggregation / range contains partial data, skipping range aggregation.");
        }

        return retVal;
    }

    private void setupAggregationsOrdering(RangeAggregationBuilder range,
                                           DateHistogramAggregationBuilder timeInterval,
                                           ElasticMetricGroupByAggregations termsGroupBy,
                                           SearchSourceBuilder sourceBuilder) throws DalException {

        Boolean hasRangeAgg   = range != null;
        Boolean hasGroupByAgg = termsGroupBy != null;
        Boolean hasTimeAgg    = timeInterval != null;

        //
        // Order of importance:
        //  1. First GroupBy aggregation
        //  2. Last GroupBy aggregation (if exists)
        //  3. Range aggregation / Time Interval aggregation - cannot exist together
        //  4. Metric Stats aggregation
        //
        AbstractAggregationBuilder lastAggAdded = null;

        if(hasRangeAgg || hasTimeAgg) {
            if(hasRangeAgg) {
                lastAggAdded = addStatisticsSubAggregations(range);
                String rangeIdentifier = range.getName();
                parsers.add(0, getRangeAggregationParser(rangeIdentifier));
                LOGGER.debug(String.format("Attached RANGE aggregation parser [%s]", rangeIdentifier));
            } else {
                lastAggAdded = addStatisticsSubAggregations(timeInterval);
                String dateHistIdentifier = timeInterval.getName();
                parsers.add(0, getTimeIntervalAggregationParser(dateHistIdentifier));
                LOGGER.debug(String.format("Attached DATE HISTOGRAM aggregation parser [%s]", dateHistIdentifier));
            }
        }

        if(hasGroupByAgg) {
            // Multiple groupBy aggregations are already sub-linked between them,
            // the connection was made during their creation
            List<TermsAggregationBuilder> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();

            if(groupByAggsOrder != null && groupByAggsOrder.isEmpty() == false) {
                TermsAggregationBuilder firstGroupByAgg = groupByAggsOrder.get(0);
                TermsAggregationBuilder lastGroupByAgg  = groupByAggsOrder.get(groupByAggsOrder.size() - 1);

                if(lastAggAdded != null) {
                    // Statistics aggregations were already added at range / time interval aggregation creation
                    lastGroupByAgg.subAggregation(lastAggAdded);
                } else {
                    addStatisticsSubAggregations(lastGroupByAgg);
                }

                TermsAggregationBuilder agg;
                int groupByAggCount = groupByAggsOrder.size();
                for(int i = 0; i < groupByAggCount; ++i) {
                    // Get by reverse order
                    agg = groupByAggsOrder.get((groupByAggCount - 1) - i);
                    String aggIdentifier = agg.getName();

                    // Stack 'group by' parsers for every sub aggregation
                    parsers.add(0, getGroupByAggregationParser(aggIdentifier));
                    LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggIdentifier));
                }

                lastAggAdded = firstGroupByAgg;
            }
        }

        if(lastAggAdded == null) {
            // Build a simple statistic aggregation query
            lastAggAdded = createStatisticsAggregations();
        }

        sourceBuilder.aggregation(lastAggAdded);
    }
    // endregion

    // region Parsers
    protected abstract BaseMetricAggregationParser getStatsAggregationParser(String identifier);
    protected abstract BaseMetricAggregationParser getRangeAggregationParser(String identifier);
    protected abstract BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier);
    protected abstract BaseMetricAggregationParser getGroupByAggregationParser(String identifier);

    public ElasticMetricStatisticsResponse parseResponse(SearchResponse searchResponse) throws DalException {
        LOGGER.debug("Parsing elastic search SearchResponse --> EsMetricStatisticsResponse");

        ElasticMetricStatisticsResponse retVal    = new ElasticMetricStatisticsResponse();
        SearchHits                 outerHits = searchResponse.getHits();

        TotalHits totalHits = outerHits.getTotalHits();
        LOGGER.debug(String.format("Search Response Total Hits: [%s]", totalHits));

        if (totalHits.equals(0)) {
            LOGGER.debug("No matches returned from elastic search, returning an empty response");
            return retVal;
        }

        // All aggregation values should be save to the result map - linked hash map is mandatory for keeping the order on range aggregations
        Map<String, ElasticMetricAggregations> byRefResultMap = new LinkedHashMap<>();

        // Create composite key to identify parser aggregations in the first iteration
        AggCompositeKey compositeKey = new AggCompositeKey();
        compositeKey.setTermKey(IMetricAggregationParser.ORIGIN_AGG);

        // Load first parser aggregation to parse
        Map<AggCompositeKey, Aggregations> nextAggsToParse = new HashMap<>();
        nextAggsToParse.put(compositeKey, searchResponse.getAggregations());

        for (IMetricAggregationParser parser : parsers) {
            nextAggsToParse = parser.parse(nextAggsToParse, byRefResultMap);
        }

        List<ElasticMetricDimension> dimensionsToAppend = request.getDimensions();

        Boolean rangeReq        = isRangeRequest(request);
        Boolean isRangeOuterAgg = isRangeOuterAgg();
        List<String> groupByNames = request.getGroupBy() != null ?
                                    request.getGroupBy() :
                                    new ArrayList<>();

        // Add request dimensions to result structure
        if (byRefResultMap.isEmpty() == false) {
            byRefResultMap.keySet().forEach(k -> {

                // Get metric aggregation response associated with metric value key
                ElasticMetricAggregations esMetricAggregations = byRefResultMap.get(k);

                // Append dimensions only for aggregations:
                //  1. Not containing a ranged aggregation
                //  2. The outer aggregation is the ranged one
                if(rangeReq == false || (rangeReq && isRangeOuterAgg)) {
                    dimensionsToAppend.forEach(d -> {
                        String dimensionName = d.getName();

                        // Add dimension only if it differ by name
                        if(groupByNames.contains(dimensionName) == false) {
                            esMetricAggregations.addDimension(d);
                        }
                    });
                }

                // Add aggregation to final result
                retVal.addMetricAggregation(esMetricAggregations);
            });
        }

        LOGGER.debug("Successfully parsed SearchResponse --> EsMetricStatisticsResponse");

        return retVal;
    }

    private Boolean isRangeOuterAgg() {
        Boolean retVal = false;

        if(parsers != null && parsers.isEmpty() == false) {
            String aggregationName = parsers.get(0).getAggregationName();
            retVal = aggregationName.toLowerCase().equals(AGG_RANGE_NAME.toLowerCase());
        }

        return retVal;
    }

    private Boolean isRangeRequest(ElasticMetricStatisticsRequest request) {
        ElasticMetricRange range = request.getRange();
        return range != null && range.hasPartialData() == false;
    }
    // endregion

    public static Double roundFix(Double value, Integer precision) {
        Double retVal = null;

        if(value != null && precision != null) {
            Double shift = Math.pow(10, precision);
            retVal = round(value*shift) / shift;
        }

        return retVal;
    }

    public static Double round(Double value) {
        Double retVal = null;
        if(value != null) {
            retVal = Math.floor(value + .5);
        }
        return retVal;
    }
}
