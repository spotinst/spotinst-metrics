package com.spotinst.metrics.bl.index.spotinst;

import co.elastic.clients.json.JsonData;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.parsers.BaseMetricAggregationParser;
import com.spotinst.metrics.bl.parsers.IMetricAggregationParser;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.dal.models.elastic.*;
import co.elastic.clients.elasticsearch._types.Time;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.MaxAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.MinAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.BucketScriptAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.BucketSelectorAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramAggregation;
//import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramInterval;
//import co.elastic.clients.util.DateHistogramInterval;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.MetricScripts.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Namespace.*;

public abstract class BaseIndexManager<T extends ElasticMetricStatisticsRequest> implements IFilterBuilder, IAggregationBuilder, ISearchResponseParser {

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
    public void setFilters(SearchRequest.Builder searchRequestBuilder) throws DalException {
        TermsQuery       namespaceQuery     = createNamespaceQuery();
        List<TermsQuery> termsQueries       = createDimensionsQueries();
        ExistsQuery      existsQuery        = createMetricExistsQuery();
        RangeQuery       timeQuery          = createTimeQuery();
        BoolQuery        dataOwnershipQuery = createDataOwnershipQuery();

        //        BoolQuery boolQuery = QueryBuilders.boolQuery();
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        //        boolQuery.must(existsQuery);
        //        boolQueryBuilder.must(existsQuery._toQuery());
        boolQueryBuilder.must(m -> m.exists(existsQuery));

        // Use filter instead of query to benefit from elastic search cache and avoid document scoring
        //        boolQuery.filter(namespaceQuery);

        //        boolQueryBuilder.filter(namespaceQuery._toQuery());
        boolQueryBuilder.filter(f -> f.terms(namespaceQuery));
        // Filters with different keys should have the AND operator between them.
        // Example: balancer_id, target_set_id & target_id should be used with an AND operator
        //        for(TermsQuery tQuery : termsQueries) {
        //            boolQuery.filter(tQuery);
        //        }

        for (TermsQuery tQuery : termsQueries) {
            //            boolQueryBuilder.filter(tQuery._toQuery());
            boolQueryBuilder.filter(f -> f.terms(tQuery));
        }

        //        // Append account id on filter to enforce data ownership
        //        if(dataOwnershipQuery != null) {
        //            boolQuery = boolQuery.filter(dataOwnershipQuery);
        //        }

        // Append account id on filter to enforce data ownership
        if (dataOwnershipQuery != null) {
            //            boolQueryBuilder.filter(dataOwnershipQuery._toQuery());
            boolQueryBuilder.filter(f -> f.bool(dataOwnershipQuery));
        }

        if (timeQuery != null) {
            boolQueryBuilder.filter(f -> f.range(timeQuery));
        }

                BoolQuery boolQuery = boolQueryBuilder.build();
        searchRequestBuilder.query(q -> q.bool(boolQuery));
        //        searchRequestBuilder.source(s -> s.size(0));
        //                sourceBuilder.query(boolQuery);

        // Do not return documents, we care about the aggregation content
        //        sourceBuilder.size(0);
        searchRequestBuilder.size(0);
    }

    protected abstract BoolQuery createDataOwnershipQuery();

    private List<TermsQuery> createDimensionsQueries() throws DalException {
        List<TermsQuery>             retVal;
        List<ElasticMetricDimension> dimensions = request.getDimensions();

        if (CollectionUtils.isEmpty(dimensions) == false) {
            retVal = new ArrayList<>();

            LOGGER.debug("Starting to report match query filter...");
            // Filters with the same key should have the OR operator between them.
            // Example: multiple dimensions with different values e.g target_id with id-123 and id-234
            Map<String, List<Object>> sameDimensionQueries = new HashMap<>();

            dimensions.forEach(d -> {
                String path           = getDimensionQueryFieldPath(d.getName());
                String dimensionValue = d.getValue();

                // If we've already have a query with the same path - append, else add
                if (sameDimensionQueries.containsKey(path)) {
                    List<Object> toAppend = sameDimensionQueries.get(path);
                    toAppend.add(dimensionValue);
                }
                else {
                    List<Object> toAdd = new ArrayList<>();
                    toAdd.add(dimensionValue);
                    sameDimensionQueries.put(path, toAdd);
                }
            });

            //            for(String dimensionKey : sameDimensionQueries.keySet()) {
            for (Map.Entry<String, List<Object>> entry : sameDimensionQueries.entrySet()) {
                String           dimensionKey    = entry.getKey();
                List<FieldValue> dimensionValues = entry.getValue().stream().map(FieldValue::of).toList();

                //                List<Object> sameDimensionValues = sameDimensionQueries.get(dimensionKey);
                //Need to check below:
                //                List<FieldValue> fieldValues = new ArrayList<>();
                TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
                termsQueryBuilder.field(dimensionKey);
                termsQueryBuilder.terms(t -> t.value(dimensionValues));
                TermsQuery termsQuery = termsQueryBuilder.build();
                retVal.add(termsQuery);

                //                for (Object value : sameDimensionValues) {
                //                    fieldValues.add(FieldValue.of(value.toString()));
                //                }
                //                TermsQuery terms = new TermsQuery(dimensionKey, sameDimensionValues);
                //                termsQueryBuilder.terms(t -> t.value(entry.getValue()));
                //                        .terms(new TermsQueryField.Builder().value(fieldValues).build())
                //                        .build();
                //                TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
                //                termsQueryBuilder.field(dimensionKey);
                //                termsQueryBuilder.terms(t -> t.value(sameDimensionValues));
                //                TermsQuery terms = termsQueryBuilder.build();

                //                retVal.add(terms);
            }

            LOGGER.debug(String.format("Created terms queries for [%s] dimensions", sameDimensionQueries.size()));
        }
        else {
            String errMsg =
                    "Missing dimensions to report terms queries, must have at least one dimension. Cannot report filter";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        return retVal;
    }

    private TermsQuery createNamespaceQuery() throws DalException {
        TermsQuery  retVal;
        Set<String> namespaces   = new HashSet<>();
        String      reqNamespace = request.getNamespace();

        if (StringUtils.isEmpty(reqNamespace)) {
            String errMsg = "[namespace] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }
        else {
            namespaces.add(reqNamespace);
            //TODO Tal : I think we can delete it
            //            if(reqNamespace.toLowerCase().equals(BALANCER_NAMESPACE_OLD)) {
            //                namespaces.add(BALANCER_NAMESPACE_NEW);
            //            }
            //            else if(reqNamespace.toLowerCase().equals(SYSTEM_NAMESPACE_OLD)) {
            //                namespaces.add(SYSTEM_NAMESPACE_NEW);
            //            }
        }

        List<FieldValue> namespaceValues = namespaces.stream().map(FieldValue::of).toList();

        TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
        termsQueryBuilder.field(NAMESPACE_FIELD_PATH);
        termsQueryBuilder.terms(t -> t.value(namespaceValues));
        TermsQuery termsQuery = termsQueryBuilder.build();

        //        retVal = new TermsQueryBuilder(NAMESPACE_FIELD_PATH + METRIC_KEYWORD_SUFFIX, namespaces);
        //        retVal = new TermsQuery(NAMESPACE_FIELD_PATH, namespaces);


        List<FieldValue> fieldValues = new ArrayList<>();
        for (String namespace : namespaces) {
            fieldValues.add(FieldValue.of(namespace));
        }

        retVal = new TermsQuery.Builder().field(NAMESPACE_FIELD_PATH)
                                         .terms(new TermsQueryField.Builder().value(fieldValues).build()).build();

        return retVal;
    }

    private ExistsQuery createMetricExistsQuery() throws DalException {
        //        ExistsQuery metricExistQuery;
        String metricName = request.getMetricName();

        if (StringUtils.isEmpty(metricName)) {
            String errMsg = "[metricName] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        LOGGER.debug(String.format("Created exists query on metric [%s]", metricName));
        String path = getMetricExistsFieldPath();
        //        metricExistQuery = QueryBuilders.existsQuery(path);

        ExistsQuery.Builder existsQueryBuilder = new ExistsQuery.Builder();
        existsQueryBuilder.field(path);
        ExistsQuery existsQuery = existsQueryBuilder.build();

        return existsQuery;
    }

    //TODO Tal: Has to handle it
    private RangeQuery createTimeQuery() throws DalException {
        //        RangeQuery      rangeQuery = QueryBuilders.rangeQuery(TIMESTAMP_FIELD_PATH);
        //        RangeQuery rangeQuery;
        RangeQuery             retVal    = null;
        ElasticMetricDateRange dateRange = request.getDateRange();

        if (dateRange != null && dateRange.getFrom() != null && dateRange.getTo() != null) {
            LOGGER.debug("Starting to report query based on date range...");

            Date fromDate = dateRange.getFrom();
            Date toDate   = dateRange.getTo();

            String fromDateFormatted = ElasticMetricTimeUtils.dateToString(fromDate);
            String toDateFormatted   = ElasticMetricTimeUtils.dateToString(toDate);

            //            retVal = RangeQuery.of(r -> r.field(TIMESTAMP_FIELD_PATH).gte(JsonData.of(fromDateFormatted)).lte(JsonData.of(toDateFormatted))).toQuery();
            retVal = RangeQuery.of(
                    r -> r.date(d -> d.field(TIMESTAMP_FIELD_PATH).gte(fromDateFormatted).lte(toDateFormatted)));
            //                retVal = RangeQuery.of(r -> r.date(d -> d.field(TIMESTAMP_FIELD_PATH).gte(fromDateFormatted).lte(toDateFormatted)))._toQuery();
            //
            //            DateRangeQuery.Builder dateRangeQueryBuilder = new DateRangeQuery.Builder();
            //            dateRangeQueryBuilder.field(TIMESTAMP_FIELD_PATH);
            //            dateRangeQueryBuilder.gte(fromDateFormatted);
            //            dateRangeQueryBuilder.lte(toDateFormatted);
            //            dateRangeQuery = dateRangeQueryBuilder.build();
            LOGGER.debug(
                    String.format("Finished creating query based on date range, from [%s] to [%s]", fromDate, toDate));
        }
        else {
            LOGGER.debug("Cannot report query, missing [dateRange] values");
            //            retVal = RangeQuery.of(r -> r.field(TIMESTAMP_FIELD_PATH))._toQuery();
            retVal = RangeQuery.of(r -> r.date(d -> d.field(TIMESTAMP_FIELD_PATH)));
        }

        return retVal;
    }
    // endregion

    // region Statistics
    protected abstract Aggregation createSumStatAggregation();


    //TODO Tal: Need to handle AGG_METRIC_AVERAGE_NAME either. It should be in the search request.
//    private BucketScriptAggregation createAverageStatAggregation() {
//        Map<String, String> bucketsScriptPathsMap = new HashMap<>();
//        bucketsScriptPathsMap.put(AGG_PIPELINE_SUM_VALUE, AGG_METRIC_SUM_NAME);
//        bucketsScriptPathsMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);
//
//        String avgScriptStr = String.format(AVG_BUCKET_SCRIPT_FORMAT, AGG_PIPELINE_SUM_VALUE, AGG_PIPELINE_COUNT_VALUE);
//
//        //        Script avgScript = new Script(avgScriptStr);
//
//
//        Script avgScript = new Script.Builder().source(avgScriptStr).build();
//
//
//        //        BucketScriptPipelineAggregation bucketScript = BucketScriptPipelineAggregation
//        //                .bucketScript(AGG_METRIC_AVERAGE_NAME, bucketsScriptPathsMap, avgScript);
//
//        //        BucketScriptAggregation bucketScript = new BucketScriptAggregation.Builder().bucketsPath(bucketsScriptPathsMap).script(avgScript).build();
//
//        //        BucketScriptAggregation bucketScript = new BucketScriptAggregation.Builder()
//        //                .bucketsPath(bucketsScriptPathsMap)
//        //                .script(avgScript)
//        //                .build();
//
//        BucketsPath bucketsPath = new BucketsPath.Builder().dict(bucketsScriptPathsMap).build();
//
//        BucketScriptAggregation bucketScript =
//                new BucketScriptAggregation.Builder().bucketsPath(bucketsPath).script(avgScript).build();
//
//        return bucketScript;
//    }

    private Aggregation createAverageStatAggregation() {
        String metricName = request.getMetricName();

        // Sum Aggregation
        SumAggregation sumAgg = AggregationBuilders.sum()
                                                   .field(metricName)
                                                   .build();

        // Count Aggregation
        ValueCountAggregation countAgg = AggregationBuilders.valueCount()
                                                            .field(metricName)
                                                            .build();

        // Bucket Script Aggregation
        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put(AGG_PIPELINE_SUM_VALUE, AGG_METRIC_SUM_NAME);
        bucketsPathMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);

        String avgScriptStr = String.format(AVG_BUCKET_SCRIPT_FORMAT, AGG_PIPELINE_SUM_VALUE, AGG_PIPELINE_COUNT_VALUE);

        // Create the BucketsPath object
        BucketsPath bucketsPath = new BucketsPath.Builder()
                .dict(bucketsPathMap)
                .build();

        // Create the average bucket aggregation without using scripts
        Aggregation avgBucketAggregation = AggregationBuilders.bucketScript()
                                                              .bucketsPath(bucketsPath)
                                                              .script(s -> s.source(avgScriptStr))
                                                              .build()._toAggregation();

        // Combine Aggregations
        Map<String, Aggregation> subAggregations = new HashMap<>();
        subAggregations.put("sum_agg", sumAgg._toAggregation());
        subAggregations.put("count_agg", countAgg._toAggregation());
        subAggregations.put("avg_agg", avgBucketAggregation);

        // Combine Aggregations
        Aggregation aggregation = AggregationBuilders.composite().sources(subAggregations, new HashMap<String, CompositeAggregationSource>())
                                                     .build()._toAggregation();

//        BucketScriptAggregation bucketScriptAgg = AggregationBuilders.bucketScript()
//                                                                             .bucketsPath(bucketsPathMap)
//                                                                             .script(avgScriptStr)
//                                                                             .build();
        // Bucket Script Aggregation
//        BucketsPath bucketsPath = new BucketsPath.Builder().dict(bucketsPathMap).build();
//
//        BucketScriptAggregation bucketScriptAgg = AggregationBuilders.bucketScript()
//                                                                     .bucketsPath(bucketsPath).
//                                                                     .script(avgScriptStr)
//                                                                     .build();

        // Combine Aggregations
//        Aggregation aggregation = AggregationBuilders.composite()
//                                                     .sources("sum_agg", sumAgg)
//                                                     .sources("count_agg", countAgg)
//                                                     .sources("avg_agg", bucketScriptAgg)
//                                                     .build();

        return aggregation;
    }

    //TODO Tal: Need to handle AGG_METRIC_MIN_NAME either. It should be in the search request.
    private MinAggregation createMinStatAggregation() {
        String         fieldPath = getMetricQueryFieldPath();
        MinAggregation retVal    = AggregationBuilders.min().field(fieldPath).build();
        return retVal;
    }

    //TODO Tal: Need to handle AGG_METRIC_MAX_NAME either. It should be in the search request.
    private MaxAggregation createMaxStatAggregation() {
        String         fieldPath = getMetricQueryFieldPath();
        MaxAggregation retVal    = AggregationBuilders.max().field(fieldPath).build();
        return retVal;
    }

    //TODO Tal: Need to handle AGG_METRIC_COUNT_NAME either. It should be in the search request.
    private Aggregation createCountStatAggregation() {
        Aggregation retVal;
        String metricName = request.getMetricName();
        SumAggregation.Builder sumAggBuilder = AggregationBuilders.sum();
        String fieldNameStr  = String.format(METRIC_COUNT_FIELD_PATH_FORMAT, metricName);

        sumAggBuilder.field(fieldNameStr);

        SumAggregation countAgg = sumAggBuilder.build();

        retVal = countAgg._toAggregation();
        return retVal;
    }

//    protected SumAggregation createSumStatAggregation() {
//        String                metricName = request.getMetricName();
//        SumAggregation.Builder sumAggBuilder = AggregationBuilders.sum();
//
//        String fieldNameStr = String.format(SUM_AGG_SCRIPT_FORMAT_RAW_INDEX, metricName);
//
//        sumAggBuilder.field(fieldNameStr);
//
//        SumAggregation retVal = sumAggBuilder.build();
//
//        return retVal;
//    }

    //TODO Tal: Need to handle EMPTY_COUNT_BUCKET_FILTER_NAME either. It should be in the search request.
    private BucketSelectorAggregation createBucketsFilterByCondition() {
        Map<String, String> bucketsPathsMap = new HashMap<>();
        bucketsPathsMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);

        String emptyBucketsScriptStr = String.format(EMPTY_COUNT_BUCKET_SCRIPT_FORMAT, AGG_PIPELINE_COUNT_VALUE);

        //        Script emptyBucketsScript = new Script(emptyBucketsScriptStr);
        //
        //        BucketSelectorAggregation bucketSelector =
        //                new BucketSelectorAggregation.Builder().bucketsPath(bucketsPathsMap).script(emptyBucketsScript).build();

        Script emptyBucketsScript = new Script.Builder().source(emptyBucketsScriptStr).build();

        BucketsPath bucketsPath = new BucketsPath.Builder().dict(bucketsPathsMap).build();

        BucketSelectorAggregation bucketSelector =
                new BucketSelectorAggregation.Builder().bucketsPath(bucketsPath).script(emptyBucketsScript).build();

        return bucketSelector;
    }
    // endregion

    // region Aggregations
    @Override
    public void setAggregations(SearchRequest.Builder searchRequestBuilder) throws DalException, IOException {

        // Create range aggregation
        RangeAggregation rangeAggregation = createRangeAggregationQuery();

        //        // Create terms aggregations by 'group by' values
        ElasticMetricGroupByAggregations groupByAggs = createGroupByAggregation();

        // Create date histogram aggregation by time interval & date range
        DateHistogramAggregation timeIntervalAggregation = createTimeIntervalAggregation();

        setupAggregationsOrdering(rangeAggregation, timeIntervalAggregation, groupByAggs, searchRequestBuilder);
    }

    protected abstract String getMetricQueryFieldPath();

    protected abstract String getMetricExistsFieldPath();

    protected abstract String getDimensionQueryFieldPath(String dimension);

    private Map<String, Aggregation> addStatisticsSubAggregations(Aggregation parentAggregation) {
        MetricStatisticEnum statistic   = request.getStatistic();
        Boolean             isLegalStat = true;
        String              statAggName = "";

        Map<String, Aggregation> aggregationMap = new HashMap<>();

        Aggregation.Builder parentBuilder = new Aggregation.Builder();

        switch (statistic) {
            case MINIMUM: {
                MinAggregation minStat = createMinStatAggregation();
                parentBuilder.min(m -> m.field(minStat.field()));
                statAggName = AGG_METRIC_MIN_NAME;
            }
            break;
            case MAXIMUM: {
                MaxAggregation maxStat = createMaxStatAggregation();
                parentBuilder.max(m -> m.field(maxStat.field()));
                statAggName = AGG_METRIC_MAX_NAME;
                //                parentAggregation.subAggregation(maxStat);
                //                parentAggregation = AggregationBuilders.max().field(maxStat.field()).build();
                //                parentAggregation = AggregationBuilders.min()
                //                                                       .field(maxStat.field())
                //                                                       .build()
                //                                                       ._toAggregation();
            }
            break;
            case SUM: {
                //                Aggregation sumStat = createSumStatAggregation();
                ////                parentAggregation = AggregationBuilders.sum().field(sumStat.field()).build();
                //                parentAggregation = AggregationBuilders.min()
                //                                                       .field(sumStat.field())
                //                                                       .build()
                //                                                       ._toAggregation();
                //                //                parentAggregation.subAggregation(sumStat);
                //                statAggName = AGG_METRIC_SUM_NAME;

                SumAggregation sumStat = createSumStatAggregation();
                //                parentAggregation = AggregationBuilders.sum()
                //                                                       .field(sumStat.field())
                //                                                       .build()
                //                                                       ._toAggregation();
                parentBuilder.sum(s -> s.field(sumStat.field()));
                statAggName = AGG_METRIC_SUM_NAME;
            }
            break;
            case COUNT: {
                SumAggregation countStat = createCountStatAggregation();
                //                parentAggregation = AggregationBuilders.sum().field(countStat.field()).build();
                //                parentAggregation.subAggregation(countStat);
                //                parentAggregation = AggregationBuilders.min()
                //                                                       .field(countStat.field())
                //                                                       .build()
                //                                                       ._toAggregation();
                parentBuilder.sum(s -> s.field(countStat.field()));
                statAggName = AGG_METRIC_COUNT_NAME;
            }
            break;
            case AVERAGE: {
                Aggregation          sumAgg   = createSumStatAggregation();
                aggregationMap.put(AGG_METRIC_SUM_NAME, sumAgg);
                Aggregation          countAgg = createCountStatAggregation();
                aggregationMap.put(AGG_METRIC_COUNT_NAME, countAgg);
                BucketScriptAggregation avgStat   = createAverageStatAggregation();

//                parentBuilder.sum(s -> s.field(sumStat.field()));
//                parentBuilder.sum(s -> s.field(countStat.field()));
//                parentBuilder.bucketScript(bs -> bs.bucketsPath(avgStat.bucketsPath()).script(avgStat.script()));
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

        if (isLegalStat) {
            parsers.add(0, getStatsAggregationParser(statAggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
        }

        return aggregationMap;
    }

    //TODO Tal : Version before changes 23.1
//    private Aggregation addStatisticsSubAggregations(Aggregation parentAggregation) {
//        MetricStatisticEnum statistic   = request.getStatistic();
//        Boolean             isLegalStat = true;
//        String              statAggName = "";
//
//        Aggregation.Builder parentBuilder = new Aggregation.Builder();
//
//        switch (statistic) {
//            case MINIMUM: {
//                MinAggregation minStat = createMinStatAggregation();
//                parentBuilder.min(m -> m.field(minStat.field()));
//                statAggName = AGG_METRIC_MIN_NAME;
//                //                parentAggregation.subAggregation(minStat);
//                //                parentAggregation = AggregationBuilders.min().field(minStat.field()).build();
//                //                parentAggregation = AggregationBuilders.min()
//                //                                                       .field(minStat.field())
//                //                                                       .build()
//                //                                                       ._toAggregation();
//            }
//            break;
//            case MAXIMUM: {
//                MaxAggregation maxStat = createMaxStatAggregation();
//                parentBuilder.max(m -> m.field(maxStat.field()));
//                statAggName = AGG_METRIC_MAX_NAME;
//                //                parentAggregation.subAggregation(maxStat);
//                //                parentAggregation = AggregationBuilders.max().field(maxStat.field()).build();
//                //                parentAggregation = AggregationBuilders.min()
//                //                                                       .field(maxStat.field())
//                //                                                       .build()
//                //                                                       ._toAggregation();
//            }
//            break;
//            case SUM: {
//                //                Aggregation sumStat = createSumStatAggregation();
//                ////                parentAggregation = AggregationBuilders.sum().field(sumStat.field()).build();
//                //                parentAggregation = AggregationBuilders.min()
//                //                                                       .field(sumStat.field())
//                //                                                       .build()
//                //                                                       ._toAggregation();
//                //                //                parentAggregation.subAggregation(sumStat);
//                //                statAggName = AGG_METRIC_SUM_NAME;
//
//                SumAggregation sumStat = createSumStatAggregation();
//                //                parentAggregation = AggregationBuilders.sum()
//                //                                                       .field(sumStat.field())
//                //                                                       .build()
//                //                                                       ._toAggregation();
//                parentBuilder.sum(s -> s.field(sumStat.field()));
//                statAggName = AGG_METRIC_SUM_NAME;
//            }
//            break;
//            case COUNT: {
//                SumAggregation countStat = createCountStatAggregation();
//                //                parentAggregation = AggregationBuilders.sum().field(countStat.field()).build();
//                //                parentAggregation.subAggregation(countStat);
//                //                parentAggregation = AggregationBuilders.min()
//                //                                                       .field(countStat.field())
//                //                                                       .build()
//                //                                                       ._toAggregation();
//                parentBuilder.sum(s -> s.field(countStat.field()));
//                statAggName = AGG_METRIC_COUNT_NAME;
//            }
//            break;
//            case AVERAGE: {
//                SumAggregation          sumStat   = createSumStatAggregation();
//                SumAggregation          countStat = createCountStatAggregation();
//                BucketScriptAggregation avgStat   = createAverageStatAggregation();
//
//                parentBuilder.sum(s -> s.field(sumStat.field()));
//                parentBuilder.sum(s -> s.field(countStat.field()));
//                parentBuilder.bucketScript(bs -> bs.bucketsPath(avgStat.bucketsPath()).script(avgStat.script()));
//                statAggName = AGG_METRIC_AVERAGE_NAME;
//            }
//            break;
//            default: {
//                isLegalStat = false;
//                String format = "Statistics [%s] is not supported, cannot report aggregation based on statistic.";
//                String errMsg = String.format(format, statistic);
//                LOGGER.error(errMsg);
//            }
//            break;
//        }
//
//        if (isLegalStat) {
//            parsers.add(0, getStatsAggregationParser(statAggName));
//            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
//        }
//
//        return parentAggregation;
//    }

    //    private Aggregation createStatisticsAggregations() throws DalException {
    //        Aggregation retVal      = null;
    //        MetricStatisticEnum        statistic   = request.getStatistic();
    //        Boolean                    isLegalStat = true;
    //        String                     statAggName = "";
    //        String                     errMsg      = "";
    //
    //        switch (statistic) {
    //            case MINIMUM: {
    ////                retVal = createMinStatAggregation();
    //                MinAggregation minStat = createMinStatAggregation();
    //                retVal = AggregationBuilders.min()
    //                                            .field(minStat.field())
    //                                            .build()
    //                                            ._toAggregation();
    //                statAggName = AGG_METRIC_MIN_NAME;
    //            }
    //            break;
    //            case MAXIMUM: {
    //                retVal = createMaxStatAggregation();
    //            }
    //            break;
    //            case SUM: {
    //                retVal = createSumStatAggregation();
    //            }
    //            break;
    //            case COUNT: {
    //                retVal = createCountStatAggregation();
    //            }
    //            break;
    //            case AVERAGE: {
    //                isLegalStat = false;
    //                //                AbstractAggregationBuilder         sumStat   = createSumStatAggregation();
    //                //                AbstractAggregationBuilder         countStat = createCountStatAggregation();
    //                //                AbstractPipelineAggregationBuilder avgStat   = createAverageStatAggregation();
    //
    //                // TODO: need to complete the way we're serving statistics on requests without parent aggregation
    //
    //                statAggName = AGG_METRIC_AVERAGE_NAME;
    //
    //                errMsg = "Cannot query for [average] statistic without timeInterval or range fields.";
    //            }
    //            break;
    //            default: {
    //                isLegalStat = false;
    //                String format = "Statistics [%s] is not supported, cannot report aggregation based on statistic.";
    //                errMsg = String.format(format, statistic);
    //                LOGGER.error(errMsg);
    //            }
    //            break;
    //        }
    //
    //        if(isLegalStat) {
    //            parsers.add(0, getStatsAggregationParser(statAggName));
    //            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
    //        } else {
    //            LOGGER.error(errMsg);
    //            throw new DalException(errMsg);
    //        }
    //
    //        return retVal;
    //    }

    private Aggregation createStatisticsAggregations() throws DalException {
        Aggregation         retVal      = null;
        MetricStatisticEnum statistic   = request.getStatistic();
        Boolean             isLegalStat = true;
        String              statAggName = "";
        String              errMsg      = "";

        switch (statistic) {
            case MINIMUM: {
                MinAggregation minStat = createMinStatAggregation();
                retVal = AggregationBuilders.min().field(minStat.field()).build()._toAggregation();
                statAggName = AGG_METRIC_MIN_NAME;
            }
            break;
            case MAXIMUM: {
                MaxAggregation maxStat = createMaxStatAggregation();
                retVal = AggregationBuilders.max().field(maxStat.field()).build()._toAggregation();
                statAggName = AGG_METRIC_MAX_NAME;
            }
            break;
            case SUM: {
                SumAggregation sumStat = createSumStatAggregation();
                retVal = AggregationBuilders.sum().field(sumStat.field()).build()._toAggregation();
                statAggName = AGG_METRIC_SUM_NAME;
            }
            break;
            case COUNT: {
                SumAggregation countStat = createCountStatAggregation();
                retVal = AggregationBuilders.sum().field(countStat.field()).build()._toAggregation();
                statAggName = AGG_METRIC_COUNT_NAME;
            }
            break;
            case AVERAGE: {
                isLegalStat = false;
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

        if (isLegalStat) {
            parsers.add(0, getStatsAggregationParser(statAggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
        }
        else {
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        return retVal;
    }

    //TODO Tal: Need to verify this function
    private DateHistogramAggregation createTimeIntervalAggregation() {
        DateHistogramAggregation dateAgg      = null;
        MetricsTimeIntervalEnum  timeInterval = request.getTimeInterval();

        if (timeInterval != null) {
            String timeIntervalStr = timeInterval.getName();
            LOGGER.debug(String.format("Starting to report time interval aggregation of [%s]...", timeInterval));

            // Initialize the Date Histogram Aggregation Builder
            DateHistogramAggregation.Builder dateAggBuilder =
                    AggregationBuilders.dateHistogram().field(TIMESTAMP_FIELD_PATH).minDocCount(1);

            // Calculate and Set the Offset
            int fromDate = ElasticMetricTimeUtils.getMinuteOfDate(request.getDateRange().getFrom());
            String offsetStr = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, fromDate);
            Time offset = Time.of(t -> t.time(offsetStr));
            dateAggBuilder.offset(offset);

            Time interval = Time.of(t -> t.time(timeIntervalStr.toLowerCase()));
            dateAggBuilder.fixedInterval(interval);

            dateAgg = dateAggBuilder.build();

            LOGGER.debug(String.format("Finished creating time interval aggregation of [%s]", timeInterval));
        }
        else {
            LOGGER.debug("No time interval values for metrics aggregation, skipping time interval aggregation.");
        }

        return dateAgg;
    }
    //    private DateHistogramAggregation createTimeIntervalAggregation() {
    //        DateHistogramAggregation dateAgg      = null;
    //        MetricsTimeIntervalEnum         timeInterval = request.getTimeInterval();
    //
    //        if(timeInterval != null) {
    //            String timeIntervalStr = timeInterval.getName();
    //            LOGGER.debug(String.format("Starting to report time interval aggregation of [%s]...", timeInterval));
    //
    ////            // Add date histogram aggregation
    ////            DateHistogramAggregation.Builder dateAggBuilder = AggregationBuilders.dateHistogram();
    ////            dateAgg.field(TIMESTAMP_FIELD_PATH);
    //////
    //////            // Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
    ////            dateAgg.minDocCount(1);
    //            DateHistogramAggregation.Builder dateAggBuilder = AggregationBuilders.dateHistogram()
    //                                                                                 .field(TIMESTAMP_FIELD_PATH)
    //                                                                                 .minDocCount(1);
    //            // Initialize the Date Histogram Aggregation Builder
    //
    //            // Set the Field
    //
    //            // Set Minimum Document Count
    //
    //            // Calculate and Set the Offset
    ////            String offset = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, ElasticMetricTimeUtils.getMinuteOfDate(request.getDateRange().getFrom()));
    ////            dateAggBuilder.offset(offset);
    //            String offsetStr = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, ElasticMetricTimeUtils.getMinuteOfDate(request.getDateRange().getFrom()));
    //            Time offset = Time.of(t -> t.time(offsetStr));
    //            dateAggBuilder.offset(offset);
    //
    //
    //            // Set the Calendar Interval
    //            DateHistogramInterval dhInterval = new DateHistogramInterval(timeIntervalStr);
    //            dateAggBuilder.calendarInterval(dhInterval);
    //
    //            // Build the Aggregation
    //            dateAgg = dateAggBuilder.build();
    //
    //
    //            // Get the minute of the dateRange 'from' value and add it as a positive offset to the date histogram agg
    //            // Histogram returns the correct results but each key represent a round up value
    //            // Example of 1 HOUR and 3 PERIODS starting from 13:15 -
    //            //   - Without offset we'll receive histograms of 11:00, 12:00, 13:00.
    //            //     The 13:00 histogram include the values between 13:00-13:15.
    //            //   - We add an offset in order to change the histogram keys to show the exact time requested i.g 11:15, 12:15, 13:15
    //
    //            ElasticMetricDateRange dateRange    = request.getDateRange();
    //            Date              fromDate     = dateRange.getFrom();
    //            int               minuteOfHour = ElasticMetricTimeUtils.getMinuteOfDate(fromDate);
    //            String            offset       = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, minuteOfHour);
    //
    //            DateHistogramInterval dhInterval = new DateHistogramInterval(timeIntervalStr);
    //            dateAgg.offset(offset);
    //            dateAgg.dateHistogramInterval(dhInterval);
    //
    //            LOGGER.debug(String.format("Finished creating time interval aggregation of [%s]", timeInterval));
    //        } else {
    //            LOGGER.debug("No time interval values for metrics aggregation, skipping time interval aggregation.");
    //        }
    //
    //        return dateAgg;
    //    }

    //TODO Tal: Need to verify the logic
    private ElasticMetricGroupByAggregations createGroupByAggregation() throws IOException {

        ElasticMetricGroupByAggregations retVal            = null;
        List<String>                     groupByDimensions = request.getGroupBy();

        if (CollectionUtils.isEmpty(groupByDimensions) == false) {
            LOGGER.debug("Starting to report [groupBy] aggregation...");

            List<TermsAggregation> groupByAggsOrder = new ArrayList<>();
            Aggregation            lastAgg          = null;

            for (String groupByDimension : groupByDimensions) {
                String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
                String aggName       = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);

                //                    TermsAggregation iterationAgg = new TermsAggregation.Builder()
                //                            .field(dimensionPath)
                //                            .minDocCount(1).build();

                TermsAggregation.Builder iterationAggBuilder = new TermsAggregation.Builder();
                iterationAggBuilder.field(dimensionPath);
                iterationAggBuilder.minDocCount(1);
                TermsAggregation iterationAgg = iterationAggBuilder.build();

                if (lastAgg != null) {
                    Aggregation finalLastAgg = lastAgg;

                    TermsAggregation.Builder termsBuilder = new TermsAggregation.Builder();
                    termsBuilder.field(dimensionPath);
                    termsBuilder.minDocCount(1);

                    Aggregation.Builder aggregationBuilder = new Aggregation.Builder();
                    aggregationBuilder.terms(termsBuilder.build());
                    aggregationBuilder.aggregations(aggName.toUpperCase(), finalLastAgg);

                    lastAgg = aggregationBuilder.build();
                    //                        Aggregation finalLastAgg = lastAgg;
                    //                        lastAgg = Aggregation.of(a -> a
                    //                                                         .terms(t -> t
                    //                                                                 .field(dimensionPath)
                    //                                                                 .minDocCount(1))
                    //                                                         .aggregations(aggName.toUpperCase(), finalLastAgg));
                    //                        Aggregation finalLastAgg = lastAgg;
                    //                        Aggregation newAgg       = Aggregation.of(a -> a.terms(t -> t.field(dimensionPath).minDocCount(1)).aggregations(aggName.toUpperCase(),
                    //                                                                                                                                        finalLastAgg));
                    //                        lastAgg = newAgg;
                }

                lastAgg = iterationAgg._toAggregation();

                // Store the group-by aggregation order for deciding the parsers order later on
                groupByAggsOrder.add(iterationAgg);
            }

            retVal = new ElasticMetricGroupByAggregations(groupByAggsOrder);
            LOGGER.debug("Finished creating group by aggregation");
        }
        else {
            LOGGER.debug("No group by values for metrics aggregation, skipping group by aggregation.");
        }

        return retVal;
    }

    private RangeAggregation createRangeAggregationQuery() throws DalException {

        RangeAggregation   retVal = null;
        ElasticMetricRange range  = request.getRange();

        if (range != null && range.hasPartialData() == false) {
            LOGGER.debug("Starting to report range aggregation...");

            String metricQueryFieldPath = getMetricQueryFieldPath();

            // Create range aggregation
            //            retVal = new RangeAggregation(AGG_RANGE_NAME);
            //            retVal.field(metricQueryFieldPath);
            RangeAggregation.Builder rangeAggBuilder = new RangeAggregation.Builder().field(metricQueryFieldPath);

            Double minimum = range.getMinimum();
            Double maximum = range.getMaximum();
            Double gap     = range.getGap();

            if (gap == 0 || (minimum == 0 && maximum == 0)) {
                String errMsg = "Cannot report range aggregation when min;max;gap values are 0";
                LOGGER.error(errMsg);
                throw new DalException(errMsg);
            }
            else {
                Double from = roundFix(minimum, 10);
                Double to;
                for (Double i = from; i <= maximum; ) {
                    to = roundFix(i + gap, 10);
                    //TODO Tal: Need to add one of the below
                    //                    retVal.addRange(i, to);
                    //                    rangeAggBuilder.addRange(r -> r.from(i).to(to));
                    i = to;
                }
                String msgFormat =
                        "Finished creating range aggregation for value [%s]: minimum [%s] maximum [%s] with a gap of [%s]";
                LOGGER.debug(String.format(msgFormat, metricQueryFieldPath, minimum, maximum, gap));
            }
        }
        else {
            LOGGER.debug(
                    "No range values for metrics aggregation / range contains partial data, skipping range aggregation.");
        }

        return retVal;
    }

    private void setupAggregationsOrdering(RangeAggregation range, DateHistogramAggregation timeInterval,
                                           ElasticMetricGroupByAggregations termsGroupBy,
                                           SearchRequest.Builder searchRequestBuilder) throws DalException {

        Boolean hasRangeAgg   = range != null;
        Boolean hasGroupByAgg = termsGroupBy != null;
        Boolean hasTimeAgg    = timeInterval != null;
        String  lastAggName   = null;

        //
        // Order of importance:
        //  1. First GroupBy aggregation
        //  2. Last GroupBy aggregation (if exists)
        //  3. Range aggregation / Time Interval aggregation - cannot exist together
        //  4. Metric Stats aggregation
        //
        //            Aggregation.Builder lastAggAdded = null;
        Map<String, Aggregation> aggregationsMap = new HashMap<>();
        Aggregation lastAggAdded = null;

        if (hasRangeAgg || hasTimeAgg) {
            if (hasRangeAgg) {
                aggregationsMap = addStatisticsSubAggregations(range._toAggregation());
                //                    String rangeIdentifier = range.getName();
                //                    String rangeIdentifier = range._toAggregation()._kind().name(); //need to verify it
                lastAggName = range._toAggregation()._kind().name();
                parsers.add(0, getRangeAggregationParser(lastAggName));
                LOGGER.debug(String.format("Attached RANGE aggregation parser [%s]", lastAggName));
            }
            else {
                aggregationsMap = addStatisticsSubAggregations(timeInterval._toAggregation());
                //                    String dateHistIdentifier = timeInterval.getName();
                lastAggName = timeInterval._toAggregation()._kind().name();
                parsers.add(0, getTimeIntervalAggregationParser(lastAggName));
                LOGGER.debug(String.format("Attached DATE HISTOGRAM aggregation parser [%s]", lastAggName));
            }
        }

        if (hasGroupByAgg) {
            // Multiple groupBy aggregations are already sub-linked between them,
            // the connection was made during their creation
            //                List<TermsAggregationBuilder> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();
            List<TermsAggregation> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();

            if (CollectionUtils.isEmpty(groupByAggsOrder) == false) {
                //                    TermsAggregationBuilder firstGroupByAgg = groupByAggsOrder.get(0);
                TermsAggregation firstGroupByAgg = groupByAggsOrder.get(0);
                //                    TermsAggregationBuilder lastGroupByAgg  = groupByAggsOrder.get(groupByAggsOrder.size() - 1);
                TermsAggregation lastGroupByAgg = groupByAggsOrder.get(groupByAggsOrder.size() - 1);

                if (lastAggAdded != null) {
                    // Statistics aggregations were already added at range / time interval aggregation creation
                    //                        lastGroupByAgg.subAggregation(lastAggAdded);
                    //                        lastGroupByAgg.aggregations(lastAggAdded);
                    Map<String, Aggregation> subAggs = new HashMap<>();
                    subAggs.put(lastAggName, lastAggAdded);

                    //                        Aggregation.Builder lastGroupByBuilder = new Aggregation.Builder().terms(lastGroupByAgg._toAggregation().terms()).aggregations(subAggs);
                    Aggregation lastGroupByAggregation = new Aggregation.Builder().terms(
                            t -> t.field(lastGroupByAgg._toAggregation().terms().field())
                                  .size(lastGroupByAgg._toAggregation().terms().size())).aggregations(subAggs).build();
                    lastAggAdded = lastGroupByAggregation;
                    //                        lastGroupByAgg._toAggregation().aggregations(lastAggAdded);
                }
                else {
                    //                        addStatisticsSubAggregations(lastGroupByAgg);
                    addStatisticsSubAggregations(lastGroupByAgg._toAggregation());
                }

                //                    TermsAggregationBuilder agg;
                TermsAggregation agg;
                int              groupByAggCount = groupByAggsOrder.size();
                for (int i = 0; i < groupByAggCount; ++i) {
                    // Get by reverse order
                    //                        agg = groupByAggsOrder.get((groupByAggCount - 1) - i);
                    agg = groupByAggsOrder.get((groupByAggCount - 1) - i);
                    //                        String aggIdentifier = agg.getName();
                    String aggIdentifier = agg._toAggregation()._kind().name();

                    // Stack 'group by' parsers for every sub aggregation
                    parsers.add(0, getGroupByAggregationParser(aggIdentifier));
                    LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggIdentifier));
                }

                //                    lastAggAdded = firstGroupByAgg;
                lastAggName = firstGroupByAgg._toAggregation()._kind().name();
                lastAggAdded = firstGroupByAgg._toAggregation();
            }
        }
        //TODO Tal: the if in comments for testing
//        if (lastAggAdded == null) {
//            // Build a simple statistic aggregation query
//            lastAggAdded = createStatisticsAggregations();
//            lastAggName = lastAggAdded._kind().name();
//        }

        //            sourceBuilder.aggregation(lastAggAdded);
//        searchRequestBuilder.aggregations(lastAggName, lastAggAdded);
        searchRequestBuilder.aggregations(aggregationsMap);
    }

    //TODO Tal : Version before changes 23.1
//    private void setupAggregationsOrdering(RangeAggregation range, DateHistogramAggregation timeInterval,
//                                           ElasticMetricGroupByAggregations termsGroupBy,
//                                           SearchRequest.Builder searchRequestBuilder) throws DalException {
//
//        Boolean hasRangeAgg   = range != null;
//        Boolean hasGroupByAgg = termsGroupBy != null;
//        Boolean hasTimeAgg    = timeInterval != null;
//        String  lastAggName   = null;
//
//        //
//        // Order of importance:
//        //  1. First GroupBy aggregation
//        //  2. Last GroupBy aggregation (if exists)
//        //  3. Range aggregation / Time Interval aggregation - cannot exist together
//        //  4. Metric Stats aggregation
//        //
//        //            Aggregation.Builder lastAggAdded = null;
//        Aggregation lastAggAdded = null;
//
//        if (hasRangeAgg || hasTimeAgg) {
//            if (hasRangeAgg) {
//                lastAggAdded = addStatisticsSubAggregations(range._toAggregation());
//                //                    String rangeIdentifier = range.getName();
//                //                    String rangeIdentifier = range._toAggregation()._kind().name(); //need to verify it
//                lastAggName = range._toAggregation()._kind().name();
//                parsers.add(0, getRangeAggregationParser(lastAggName));
//                LOGGER.debug(String.format("Attached RANGE aggregation parser [%s]", lastAggName));
//            }
//            else {
//                lastAggAdded = addStatisticsSubAggregations(timeInterval._toAggregation());
//                //                    String dateHistIdentifier = timeInterval.getName();
//                lastAggName = timeInterval._toAggregation()._kind().name();
//                parsers.add(0, getTimeIntervalAggregationParser(lastAggName));
//                LOGGER.debug(String.format("Attached DATE HISTOGRAM aggregation parser [%s]", lastAggName));
//            }
//        }
//
//        if (hasGroupByAgg) {
//            // Multiple groupBy aggregations are already sub-linked between them,
//            // the connection was made during their creation
//            //                List<TermsAggregationBuilder> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();
//            List<TermsAggregation> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();
//
//            if (CollectionUtils.isEmpty(groupByAggsOrder) == false) {
//                //                    TermsAggregationBuilder firstGroupByAgg = groupByAggsOrder.get(0);
//                TermsAggregation firstGroupByAgg = groupByAggsOrder.get(0);
//                //                    TermsAggregationBuilder lastGroupByAgg  = groupByAggsOrder.get(groupByAggsOrder.size() - 1);
//                TermsAggregation lastGroupByAgg = groupByAggsOrder.get(groupByAggsOrder.size() - 1);
//
//                if (lastAggAdded != null) {
//                    // Statistics aggregations were already added at range / time interval aggregation creation
//                    //                        lastGroupByAgg.subAggregation(lastAggAdded);
//                    //                        lastGroupByAgg.aggregations(lastAggAdded);
//                    Map<String, Aggregation> subAggs = new HashMap<>();
//                    subAggs.put(lastAggName, lastAggAdded);
//
//                    //                        Aggregation.Builder lastGroupByBuilder = new Aggregation.Builder().terms(lastGroupByAgg._toAggregation().terms()).aggregations(subAggs);
//                    Aggregation lastGroupByAggregation = new Aggregation.Builder().terms(
//                            t -> t.field(lastGroupByAgg._toAggregation().terms().field())
//                                  .size(lastGroupByAgg._toAggregation().terms().size())).aggregations(subAggs).build();
//                    lastAggAdded = lastGroupByAggregation;
//                    //                        lastGroupByAgg._toAggregation().aggregations(lastAggAdded);
//                }
//                else {
//                    //                        addStatisticsSubAggregations(lastGroupByAgg);
//                    addStatisticsSubAggregations(lastGroupByAgg._toAggregation());
//                }
//
//                //                    TermsAggregationBuilder agg;
//                TermsAggregation agg;
//                int              groupByAggCount = groupByAggsOrder.size();
//                for (int i = 0; i < groupByAggCount; ++i) {
//                    // Get by reverse order
//                    //                        agg = groupByAggsOrder.get((groupByAggCount - 1) - i);
//                    agg = groupByAggsOrder.get((groupByAggCount - 1) - i);
//                    //                        String aggIdentifier = agg.getName();
//                    String aggIdentifier = agg._toAggregation()._kind().name();
//
//                    // Stack 'group by' parsers for every sub aggregation
//                    parsers.add(0, getGroupByAggregationParser(aggIdentifier));
//                    LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggIdentifier));
//                }
//
//                //                    lastAggAdded = firstGroupByAgg;
//                lastAggName = firstGroupByAgg._toAggregation()._kind().name();
//                lastAggAdded = firstGroupByAgg._toAggregation();
//            }
//        }
//
//        if (lastAggAdded == null) {
//            // Build a simple statistic aggregation query
//            lastAggAdded = createStatisticsAggregations();
//            lastAggName = lastAggAdded._kind().name();
//        }
//
//        //            sourceBuilder.aggregation(lastAggAdded);
//        searchRequestBuilder.aggregations(lastAggName, lastAggAdded);
//    }
    // endregion

    // region Parsers
    protected abstract BaseMetricAggregationParser getStatsAggregationParser(String identifier);

    protected abstract BaseMetricAggregationParser getRangeAggregationParser(String identifier);

    protected abstract BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier);

    protected abstract BaseMetricAggregationParser getGroupByAggregationParser(String identifier);

    public ElasticMetricStatisticsResponse parseResponse(
            SearchResponse<ElasticMetricStatisticsResponse> searchResponse) throws DalException {
        LOGGER.debug("Parsing elastic search SearchResponse --> EsMetricStatisticsResponse");

        ElasticMetricStatisticsResponse               retVal    = new ElasticMetricStatisticsResponse();
        HitsMetadata<ElasticMetricStatisticsResponse> outerHits = searchResponse.hits();

        TotalHits totalHits = outerHits.total();
        LOGGER.debug(String.format("Search Response Total Hits: [%s]", totalHits));

        if (totalHits.value() == 0) {
            LOGGER.debug("No matches returned from elastic search, returning an empty response");
            return retVal;
        }

        // All aggregation values should be save to the result map - linked hash map is mandatory for keeping the order on range aggregations
        Map<String, ElasticMetricAggregations> byRefResultMap = new LinkedHashMap<>();

        // Create composite key to identify parser aggregations in the first iteration
        AggCompositeKey compositeKey = new AggCompositeKey();
        compositeKey.setTermKey(IMetricAggregationParser.ORIGIN_AGG);

        // Load first parser aggregation to parse
        Map<AggCompositeKey, Aggregate> nextAggsToParse = new HashMap<>();
        nextAggsToParse.put(compositeKey, searchResponse.aggregations().get(compositeKey.getTermKey()));

        for (IMetricAggregationParser parser : parsers) {
            nextAggsToParse = parser.parse(nextAggsToParse, byRefResultMap);
        }

        List<ElasticMetricDimension> dimensionsToAppend = request.getDimensions();

        Boolean rangeReq        = isRangeRequest(request);
        Boolean isRangeOuterAgg = isRangeOuterAgg();
        List<String> groupByNames = request.getGroupBy() != null ? request.getGroupBy() : new ArrayList<>();

        // Add request dimensions to result structure
        if (byRefResultMap.isEmpty() == false) {
            byRefResultMap.keySet().forEach(k -> {

                // Get metric aggregation response associated with metric value key
                ElasticMetricAggregations esMetricAggregations = byRefResultMap.get(k);

                // Append dimensions only for aggregations:
                //  1. Not containing a ranged aggregation
                //  2. The outer aggregation is the ranged one
                if (rangeReq == false || (rangeReq && isRangeOuterAgg)) {
                    dimensionsToAppend.forEach(d -> {
                        String dimensionName = d.getName();

                        // Add dimension only if it differ by name
                        if (groupByNames.contains(dimensionName) == false) {
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

        if (parsers != null && parsers.isEmpty() == false) {
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

        if (value != null && precision != null) {
            Double shift = Math.pow(10, precision);
            retVal = round(value * shift) / shift;
        }

        return retVal;
    }

    public static Double round(Double value) {
        Double retVal = null;
        if (value != null) {
            retVal = Math.floor(value + .5);
        }
        return retVal;
    }
}
