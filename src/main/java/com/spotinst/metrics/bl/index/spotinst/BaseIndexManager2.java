package com.spotinst.metrics.bl.index.spotinst;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.parsers.BaseMetricAggregationParser;
import com.spotinst.metrics.bl.parsers.IMetricAggregationParser;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricRange;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.infra.AggCompositeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_COUNT_FIELD_PATH_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Namespace.*;

@Slf4j
public abstract class BaseIndexManager2<T extends ElasticMetricStatisticsRequest> implements IFilterBuilder, IAggregationBuilder, ISearchResponseParser {

    protected T                              request;
    protected List<IMetricAggregationParser> parsers;

    public BaseIndexManager2(T request) {
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
    public void setFilters(SearchRequest.Builder srb) throws DalException {
        TermsQuery       namespaceQuery     = createNamespaceQuery();
        List<TermsQuery> termsQueries       = createDimensionsQueries();
        ExistsQuery      existsQuery        = createMetricExistsQuery();
        RangeQuery       timeQuery          = createTimeQuery();
        BoolQuery        dataOwnershipQuery = createDataOwnershipQuery();

        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();

        boolQueryBuilder.must(a -> a.exists(existsQuery));

        // Use filter instead of query to benefit from elastic search cache and avoid document scoring
        boolQueryBuilder.filter(f -> f.terms(namespaceQuery));

        // Filters with different keys should have the AND operator between them.
        // Example: balancer_id, target_set_id & target_id should be used with an AND operator
        for (TermsQuery tQuery : termsQueries) {
            boolQueryBuilder.filter(f -> f.terms(tQuery));
        }

        // Append account ID on filter to enforce data ownership
        if (dataOwnershipQuery != null) {
            boolQueryBuilder.filter(b -> b.bool(dataOwnershipQuery));
        }

        boolQueryBuilder.filter(f -> f.range(timeQuery));

        BoolQuery boolQuery = boolQueryBuilder.build();
        srb.query(f -> f.bool(boolQuery));

        // Queries should use the request cache if possible
        srb.requestCache(true);

        // Do not return documents, we care about the aggregation content
        srb.size(0);
    }

    protected abstract BoolQuery createDataOwnershipQuery();

    private List<TermsQuery> createDimensionsQueries() throws DalException {
        List<TermsQuery>             retVal;
        List<ElasticMetricDimension> dimensions = request.getDimensions();

        if (BooleanUtils.isFalse(CollectionUtils.isEmpty(dimensions))) {
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
            if (reqNamespace.equalsIgnoreCase(BALANCER_NAMESPACE_OLD)) {
                namespaces.add(BALANCER_NAMESPACE_NEW);
            }
            else if (reqNamespace.equalsIgnoreCase(SYSTEM_NAMESPACE_OLD)) {
                namespaces.add(SYSTEM_NAMESPACE_NEW);
            }
        }

        retVal = QueryBuilders.terms().field(NAMESPACE_FIELD_PATH).terms(termsBuilder -> termsBuilder.value(
                namespaces.stream().map(FieldValue::of).toList())).build();

        return retVal;
    }

    private ExistsQuery createMetricExistsQuery() throws DalException {
        ExistsQuery retVal;
        String      metricName = request.getMetricName();

        if (StringUtils.isEmpty(metricName)) {
            String errMsg = "[metricName] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        LOGGER.debug(String.format("Created exists query on metric [%s]", metricName));
        String path = getMetricExistsFieldPath();

        retVal = QueryBuilders.exists().field(path).build();

        return retVal;
    }

    private RangeQuery createTimeQuery() throws DalException {
        RangeQuery.Builder rangeQueryBuilder = QueryBuilders.range();

        ElasticMetricDateRange dateRange = request.getDateRange();

        if (dateRange != null && dateRange.getFrom() != null && dateRange.getTo() != null) {
            LOGGER.debug("Starting to create query based on date range...");

            Date fromDate = dateRange.getFrom();
            Date toDate   = dateRange.getTo();

            String fromDateFormatted = ElasticMetricTimeUtils.dateToString(fromDate);
            String toDateFormatted   = ElasticMetricTimeUtils.dateToString(toDate);

            rangeQueryBuilder.date(a -> a.field(TIMESTAMP_FIELD_PATH).gte(fromDateFormatted).lte(toDateFormatted));

            LOGGER.debug(
                    String.format("Finished creating query based on date range, from [%s] to [%s]", fromDate, toDate));
        }
        else {
            LOGGER.debug("Cannot create query, missing [dateRange] values");
        }

        return rangeQueryBuilder.build();
    }
    // endregion

    // region Statistics
    protected abstract SumAggregation createSumStatAggregation();

    //todo tal oyar - Seems like no need for this anymore we can just use the AverageAggregation which will do this for us.
    //    private Aggregation createAverageStatAggregation2() {
    //        String metricName = request.getMetricName();
    //
    //        // Sum Aggregation
    //        SumAggregation sumAgg = AggregationBuilders.sum()
    //                                                   .field(metricName)
    //                                                   .build();
    //
    //        // Count Aggregation
    //        ValueCountAggregation countAgg = AggregationBuilders.valueCount()
    //                                                            .field(metricName)
    //                                                            .build();
    //
    //        // Bucket Script Aggregation
    //        Map<String, String> bucketsPathMap = new HashMap<>();
    //        bucketsPathMap.put(AGG_PIPELINE_SUM_VALUE, AGG_METRIC_SUM_NAME);
    //        bucketsPathMap.put(AGG_PIPELINE_COUNT_VALUE, AGG_METRIC_COUNT_NAME);
    //
    //        String avgScriptStr = String.format(AVG_BUCKET_SCRIPT_FORMAT, AGG_PIPELINE_SUM_VALUE, AGG_PIPELINE_COUNT_VALUE);
    //
    //        // Create the BucketsPath object
    //        BucketsPath bucketsPath = new BucketsPath.Builder()
    //                .dict(bucketsPathMap)
    //                .build();
    //
    //        // Create the average bucket aggregation without using scripts
    //        Aggregation avgBucketAggregation = AggregationBuilders.bucketScript()
    //                                                              .bucketsPath(bucketsPath)
    //                                                              .script(s -> s.source(avgScriptStr))
    //                                                              .build()._toAggregation();
    //
    //        // Combine Aggregations
    //        Map<String, Aggregation> subAggregations = new HashMap<>();
    //        subAggregations.put("sum_agg", sumAgg._toAggregation());
    //        subAggregations.put("count_agg", countAgg._toAggregation());
    //        subAggregations.put("avg_agg", avgBucketAggregation);
    //
    //        // Combine Aggregations
    //        Aggregation aggregation = AggregationBuilders.composite().sources(subAggregations, new HashMap<String, CompositeAggregationSource>())
    //                                                     .build()._toAggregation();
    //
    //        return aggregation;
    //    }

    private AverageAggregation createAverageStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.avg().field(fieldPath).build();
    }

    //TODO Tal oyar : setting the name is not available anymore there is a kind on each specific aggregation so we use it.
    private MinAggregation createMinStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.min().field(fieldPath).build();
    }

    //TODO Tal oyar : setting the name is not available anymore there is a kind on each specific aggregation so we use it.
    private MaxAggregation createMaxStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.max().field(fieldPath).build();
    }

    //TODO Tal oyar :Needs to see if there is a specific aggregation for count, setting the name is not available anymore there is a kind on each specific aggregation so we use it.
    private ValueCountAggregation createCountStatAggregation() {
        ValueCountAggregation retVal;
        String                metricName = request.getMetricName();

        String field = String.format(METRIC_COUNT_FIELD_PATH_FORMAT, metricName);

        retVal = AggregationBuilders.valueCount().field(field).build();

        return retVal;
    }
    // endregion

    // region Aggregations
    @Override
    public void setAggregations(SearchRequest.Builder searchRequestBuilder) throws DalException, IOException {
        // Create range aggregation
        ElasticMetricRange               range                 = request.getRange();
        MetricsTimeIntervalEnum          timeInterval          = request.getTimeInterval();
        List<String>                     groupByDimensions     = request.getGroupBy();
        Map<String, Aggregation.Builder> finalAggBuilderByName = new HashMap<>();

        if (Objects.nonNull(range) && BooleanUtils.isFalse(range.hasPartialData())) {
            finalAggBuilderByName = createRangeAggregationQuery();
        }
        else if (Objects.nonNull(timeInterval)) {
            // Create date histogram aggregation by time interval & date range
            finalAggBuilderByName = createTimeIntervalAggregation();
        }
        else if (CollectionUtils.isNotEmpty(groupByDimensions)) {
            // Create terms aggregations by 'group by' values
            finalAggBuilderByName = createGroupByAggregations(null);
        }
        else {
            //In case no range/time interval and no groupBy aggregations, build a simple statistic aggregation query
            finalAggBuilderByName = createStatisticsAggregations();
        }

        Map.Entry<String, Aggregation.Builder> aggBuilderByNameEntry =
                finalAggBuilderByName.entrySet().stream().findFirst().orElse(null);

        if (Objects.nonNull(aggBuilderByNameEntry)) {
            searchRequestBuilder.aggregations(aggBuilderByNameEntry.getKey(), aggBuilderByNameEntry.getValue().build());
        }
    }

    protected abstract String getMetricQueryFieldPath();

    protected abstract String getMetricExistsFieldPath();

    protected abstract String getDimensionQueryFieldPath(String dimension);

    private Map<String, Aggregation.Builder> createStatisticsAggregations() throws DalException {
        Map<String, Aggregation.Builder> retVal    = new HashMap<>();
        Aggregation.Builder              builder   = new Aggregation.Builder();
        MetricStatisticEnum              statistic = request.getStatistic();
        String                           aggName   = null;

        Boolean isLegalStat = true;
        String  errMsg      = "";

        switch (statistic) {
            case MINIMUM: {
                MinAggregation minStat = createMinStatAggregation();
                builder.min(ab -> ab.field(minStat.field()));
                aggName = Aggregation.Kind.Min.name();
            }
            break;
            case MAXIMUM: {
                MaxAggregation maxStat = createMaxStatAggregation();
                builder.max(ab -> ab.field(maxStat.field()));
                aggName = Aggregation.Kind.Max.name();
            }
            break;
            case SUM: {
                SumAggregation sumStat = createSumStatAggregation();
                builder.sum(ab -> ab.field(sumStat.field()));
                aggName = Aggregation.Kind.Sum.name();
            }
            break;
            case COUNT: {
                ValueCountAggregation countStat = createCountStatAggregation();
                builder.valueCount(ab -> ab.field(countStat.field()));
                aggName = Aggregation.Kind.ValueCount.name();
            }
            break;
            case AVERAGE: {
                isLegalStat = false;

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
            retVal.put(aggName, builder);
            parsers.addFirst(getStatsAggregationParser(aggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", aggName));
        }
        else {
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        return retVal;
    }

    private Map<String, Aggregation.Builder> createRangeAggregationQuery() throws DalException {
        Map<String, Aggregation.Builder> retVal = new HashMap<>();
        Aggregation.Builder              rangeAggBuilder;
        ElasticMetricRange               range  = request.getRange();

        LOGGER.debug("Starting to create range aggregation...");

        String metricQueryFieldPath = getMetricQueryFieldPath();

        // Create range aggregation
        rangeAggBuilder = new Aggregation.Builder();

        Double minimum = range.getMinimum();
        Double maximum = range.getMaximum();
        Double gap     = range.getGap();

        List<AggregationRange> ranges = new LinkedList<>();

        if (gap == 0 || (minimum == 0 && maximum == 0)) {
            String errMsg = "Cannot create range aggregation when min;max;gap values are 0";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }
        else {
            Double from = roundFix(minimum, 10);
            Double to;

            for (Double i = from; i <= maximum; ) {
                to = roundFix(i + gap, 10);
                ranges.add(new AggregationRange.Builder().from(i).to(to).build());
                i = to;
            }

            rangeAggBuilder.range(r -> r.field(metricQueryFieldPath).ranges(ranges));

            addStatisticsSubAggregations(rangeAggBuilder);
            parsers.addFirst(getRangeAggregationParser(Aggregation.Kind.Range.name()));
            LOGGER.debug("Attached RANGE aggregation parser.");

            //Add GroupBy Aggregations
            List<String> groupByDimensions = request.getGroupBy();

            if (CollectionUtils.isNotEmpty(groupByDimensions)) {
                retVal = createGroupByAggregations(rangeAggBuilder);
            }
            else {
                retVal.put(Aggregation.Kind.Range.name(), rangeAggBuilder);
            }

            String msgFormat =
                    "Finished creating range aggregation for value [%s]: minimum [%s] maximum [%s] with a gap of [%s]";
            LOGGER.debug(String.format(msgFormat, metricQueryFieldPath, minimum, maximum, gap));
        }

        return retVal;
    }


    private Map<String, Aggregation.Builder> createTimeIntervalAggregation() {
        Map<String, Aggregation.Builder> retVal;
        MetricsTimeIntervalEnum          timeInterval           = request.getTimeInterval();
        Aggregation.Builder              timeIntervalAggBuilder = new Aggregation.Builder();

        LOGGER.debug(String.format("Starting to create time interval aggregation of [%s]...", timeInterval));

        String timeIntervalStr = timeInterval.getName();
        Time   interval        = Time.of(t -> t.time(timeIntervalStr.toLowerCase()));

        // Calculate the Offset
        int    fromDate  = ElasticMetricTimeUtils.getMinuteOfDate(request.getDateRange().getFrom());
        String offsetStr = String.format(DATE_HISTOGRAM_OFFSET_FORMAT, fromDate);
        Time   offset    = Time.of(t -> t.time(offsetStr));

        timeIntervalAggBuilder.dateHistogram(
                b -> b.field(TIMESTAMP_FIELD_PATH).offset(offset).fixedInterval(interval).minDocCount(1));

        addStatisticsSubAggregations(timeIntervalAggBuilder);
        parsers.addFirst(getTimeIntervalAggregationParser(Aggregation.Kind.DateHistogram.name()));
        LOGGER.debug("Attached RANGE aggregation parser.");

        //Add GroupBy Aggregations
        List<String> groupByDimensions = request.getGroupBy();

        if (CollectionUtils.isNotEmpty(groupByDimensions)) {
            retVal = createGroupByAggregations(timeIntervalAggBuilder);
        }
        else {
            retVal = Collections.singletonMap(Aggregation.Kind.DateHistogram.name(), timeIntervalAggBuilder);
        }

        //Add groupBy aggregations
        LOGGER.debug(String.format("Finished creating time interval aggregation of [%s]", timeInterval));

        return retVal;
    }

    private Map<String, Aggregation.Builder> createGroupByAggregations(Aggregation.Builder rangeOrTimeAggBuilder) {
        Map<String, Aggregation.Builder> retVal            = new HashMap<>();
        List<String>                     groupByDimensions = request.getGroupBy();
        List<String>                     groupByOrder      = new LinkedList<>();

        LOGGER.debug("Starting to create [groupBy] aggregation...");

        Aggregation.Builder lastAggBuilder        = null;
        int                 groupByDimensionsSize = groupByDimensions.size();
        int                 counter               = 0;
        String              aggName;

        for (String groupByDimension : groupByDimensions) {
            counter++;

            String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
            aggName = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);

            // MinDocCount - Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
            Aggregation.Builder groupByAggBuilder = new Aggregation.Builder();
            groupByAggBuilder.terms(t -> t.field(dimensionPath).minDocCount(1)).aggregations(new HashMap<>());
            groupByOrder.add(aggName);

            LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggName));

            if (Objects.equals(counter, groupByDimensionsSize)) {
                //If it's the last groupBy aggregation then connect it to the range or time interval aggregation (if exists)
                if (Objects.nonNull(rangeOrTimeAggBuilder)) {
                    Aggregation rangeOrTimeAgg = rangeOrTimeAggBuilder.build();
                    groupByAggBuilder.aggregations(rangeOrTimeAgg._kind().name(), rangeOrTimeAgg);
                }
                else {
                    //If no range or time aggregation exists, then connect the group by aggregations with the statistics aggregations
                    addStatisticsSubAggregations(groupByAggBuilder);
                }
            }

            if (lastAggBuilder != null) {
                lastAggBuilder.aggregations(aggName, groupByAggBuilder.build());
            }
            else {
                //First iteration, needs to be returned from method since this is the origin of the groupBy chain
                retVal.put(aggName, groupByAggBuilder);
            }

            lastAggBuilder = groupByAggBuilder;
        }

        //Add to the parsers list the group by aggregations in reverse order
        groupByOrder.reversed()
                    .forEach(groupByAggName -> parsers.addFirst(getGroupByAggregationParser(groupByAggName)));

        LOGGER.debug("Finished creating group by aggregation");

        return retVal;
    }

    private List<Aggregation.Builder> createGroupByAggregation2() {
        List<Aggregation.Builder> retVal            = new ArrayList<>();
        List<String>              groupByDimensions = request.getGroupBy();

        if (CollectionUtils.isNotEmpty(groupByDimensions)) {
            LOGGER.debug("Starting to create [groupBy] aggregation...");

            Aggregation.Builder lastAgg = null;

            for (String groupByDimension : groupByDimensions) {
                String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
                String aggName       = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);

                // MinDocCount - Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
                Aggregation.Builder termAgg = new Aggregation.Builder();
                termAgg.terms(t -> t.field(dimensionPath).minDocCount(1)).aggregations(new HashMap<>());

                if (lastAgg != null) {
                    lastAgg.aggregations(aggName, termAgg.build());
                }

                lastAgg = termAgg;

                // Store the group-by aggregation order for deciding the parsers order later on
                retVal.add(termAgg);
            }

            LOGGER.debug("Finished creating group by aggregation");
        }
        else {
            LOGGER.debug("No group by values for metrics aggregation, skipping group by aggregation.");
        }

        return retVal;
    }

    //    private void setupAggregationsOrdering2(Aggregation.Builder rangeAggregation,
    //                                            Aggregation.Builder dateHistogramAggregation,
    //                                            ElasticMetricGroupByAggregations termsGroupBy,
    //                                            SearchRequest.Builder srb) throws DalException {
    //
    //        Boolean hasRangeAgg   = rangeAggregation != null;
    //        Boolean hasGroupByAgg = termsGroupBy != null;
    //        Boolean hasTimeAgg    = dateHistogramAggregation != null;
    //
    //        // Order of importance:
    //        //  1. First GroupBy aggregation
    //        //  2. Last GroupBy aggregation (if exists)
    //        //  3. Range aggregation / Time Interval aggregation - cannot exist together
    //        //  4. Metric Stats aggregation
    //
    //        Aggregation.Builder lastAggBuilder = null;
    //
    //        if (hasRangeAgg || hasTimeAgg) {
    //            if (hasRangeAgg) {
    //                lastAggBuilder = addStatisticsSubAggregations(rangeAggregation);
    //                parsers.addFirst(getRangeAggregationParser(Aggregation.Kind.Range.name()));
    //                LOGGER.debug("Attached RANGE aggregation parser.");
    //            }
    //            else {
    //                lastAggBuilder = addStatisticsSubAggregations(dateHistogramAggregation);
    //                parsers.addFirst(getTimeIntervalAggregationParser(Aggregation.Kind.DateHistogram.name()));
    //                LOGGER.debug("Attached DATE HISTOGRAM aggregation parser.");
    //            }
    //        }
    //
    //        if (hasGroupByAgg) {
    //            // Multiple groupBy aggregations are already sub-linked between them,
    //            // the connection was made during their creation
    //            List<Aggregation.Builder> groupByAggsOrder = termsGroupBy.getGroupByAggsOrder();
    //
    //            if (CollectionUtils.isNotEmpty(groupByAggsOrder)) {
    //                Aggregation.Builder firstGroupByAggBuilder = groupByAggsOrder.getFirst();
    //                Aggregation.Builder lastGroupByAggBuilder  = groupByAggsOrder.getLast();
    //
    //                if (Objects.nonNull(lastAggBuilder)) {
    //                    Aggregation lastAgg = lastAggBuilder.build();
    //                    // Statistics aggregations were already added at rangeAggregation / time interval aggregation creation
    //                    lastGroupByAggBuilder.aggregations(lastAgg._kind().name(), lastAgg);
    //                }
    //                else {
    //                    addStatisticsSubAggregations(lastGroupByAggBuilder);
    //                }
    //
    //                Aggregation.Builder aggBulder;
    //                int                 groupByAggCount = groupByAggsOrder.size();
    //
    //                for (int i = 0; i < groupByAggCount; ++i) {
    //                    // Get by reverse order
    //                    aggBulder = groupByAggsOrder.get((groupByAggCount - 1) - i);
    //
    //                    String aggIdentifier = aggBulder.build()._kind().name();
    //
    //                    // Stack 'group by' parsers for every sub aggregation
    //                    parsers.addFirst(getGroupByAggregationParser(aggIdentifier));
    //                    LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggIdentifier));
    //                }
    //
    //                lastAggBuilder = firstGroupByAggBuilder;
    //            }
    //        }
    //
    //        if (lastAggBuilder == null) {
    //            // Build a simple statistic aggregation query
    //            lastAggBuilder = createStatisticsAggregations();
    //        }
    //
    //        Aggregation aggregation = lastAggBuilder.build();
    //        srb.aggregations(aggregation._kind().name(), aggregation);
    //    }

    //todo tal oyar - Need to verify this method
    //    private void setupAggregationsOrdering(Aggregation.Builder rangeAggregation,
    //                                           Aggregation.Builder dateHistogramAggregation,
    //                                           SearchRequest.Builder srb) throws DalException {
    //        // Order of importance:
    //        //  1. First GroupBy aggregation
    //        //  2. Last GroupBy aggregation (if exists)
    //        //  3. Range aggregation / Time Interval aggregation - cannot exist together
    //        //  4. Metric Stats aggregation
    //
    //        List<Aggregation.Builder> groupByAggs       = new ArrayList<>();
    //        List<String>              groupByDimensions = request.getGroupBy();
    //
    //        if (CollectionUtils.isNotEmpty(groupByDimensions)) {
    //            LOGGER.debug("Starting to create [groupBy] aggregation...");
    //
    //            Aggregation.Builder lastAgg = null;
    //
    //            for (String groupByDimension : groupByDimensions) {
    //                String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
    //                String aggName       = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);
    //
    //                // MinDocCount - Exclude all zero counts documents, do not use 0 count documents as fillers for buckets
    //                Aggregation.Builder groupByAgg = new Aggregation.Builder();
    //                groupByAgg.terms(t -> t.field(dimensionPath).minDocCount(1));
    //
    //                if (lastAgg != null) {
    //                    lastAgg.aggregations(aggName, groupByAgg.build());
    //                }
    //
    //                lastAgg = groupByAgg;
    //
    //                // Store the group-by aggregation order for deciding the parsers order later on
    //                groupByAggs.add(groupByAgg);
    //            }
    //        }
    //        else {
    //            LOGGER.debug("No group by values for metrics aggregation, skipping group by aggregation.");
    //        }
    //
    //        Aggregation finalAggregation = null;
    //        Aggregation.Builder lastAggBuilder = handleRangeOrTimeAggregations(rangeAggregation, dateHistogramAggregation);
    //        Aggregation lastAgg = lastAggBuilder.build();
    //
    //        boolean hasGroupByAgg = CollectionUtils.isNotEmpty(groupByAggs);
    //
    //        if (hasGroupByAgg) {
    //            // Multiple groupBy aggregations are already sub-linked between them,
    //            // the connection was made during their creation
    //            if (CollectionUtils.isNotEmpty(groupByAggs)) {
    //                finalAggregation = handleGroupByAggregations(groupByAggs, lastAgg);
    //            }
    //        }
    //
    //        if (Objects.isNull(finalAggregation)) {
    //            // Build a simple statistic aggregation query
    //            Aggregation.Builder statisticsAggBuilder = createStatisticsAggregations();
    //            finalAggregation = statisticsAggBuilder.build();
    //        }
    //
    //        srb.aggregations(finalAggregation._kind().name(), finalAggregation);
    //    }


    //todo tal oyar - Need to verify this method
    //    private void setupAggregationsOrdering3(Aggregation.Builder rangeAggregation,
    //                                            Aggregation.Builder dateHistogramAggregation,
    //                                            ElasticMetricGroupByAggregations termsGroupBy,
    //                                            SearchRequest.Builder srb) throws DalException {
    //        // Order of importance:
    //        //  1. First GroupBy aggregation
    //        //  2. Last GroupBy aggregation (if exists)
    //        //  3. Range aggregation / Time Interval aggregation - cannot exist together
    //        //  4. Metric Stats aggregation
    //        Aggregation finalAggregation = null;
    //        Aggregation.Builder lastAggBuilder = handleRangeOrTimeAggregations(rangeAggregation, dateHistogramAggregation);
    //        Aggregation lastAgg = lastAggBuilder.build();
    //
    //        boolean hasGroupByAgg = Objects.nonNull(termsGroupBy);
    //
    //        if (hasGroupByAgg) {
    //            // Multiple groupBy aggregations are already sub-linked between them,
    //            // the connection was made during their creation
    //            List<Aggregation.Builder> groupByAggs = termsGroupBy.getGroupByAggsOrder();
    //
    //            if (CollectionUtils.isNotEmpty(groupByAggs)) {
    //                finalAggregation = handleGroupByAggregations(groupByAggs, lastAgg);
    //            }
    //        }
    //
    //        if (Objects.isNull(finalAggregation)) {
    //            // Build a simple statistic aggregation query
    //            Aggregation.Builder statisticsAggBuilder = createStatisticsAggregations();
    //            finalAggregation = statisticsAggBuilder.build();
    //        }
    //
    //        srb.aggregations(finalAggregation._kind().name(), finalAggregation);
    //    }

    private Aggregation.Builder handleRangeOrTimeAggregations(Aggregation.Builder rangeAggregation,
                                                              Aggregation.Builder dateHistogramAggregation) {
        Aggregation.Builder lastAggBuilder      = null;
        boolean             hasRangeAgg         = Objects.nonNull(rangeAggregation);
        boolean             hasDateHistogramAgg = Objects.nonNull(dateHistogramAggregation);

        if (hasRangeAgg) {
            lastAggBuilder = addStatisticsSubAggregations(rangeAggregation);
            parsers.addFirst(getRangeAggregationParser(Aggregation.Kind.Range.name()));
            LOGGER.debug("Attached RANGE aggregation parser.");
        }
        else if (hasDateHistogramAgg) {
            lastAggBuilder = addStatisticsSubAggregations(dateHistogramAggregation);
            parsers.addFirst(getTimeIntervalAggregationParser(Aggregation.Kind.DateHistogram.name()));
            LOGGER.debug("Attached DATE HISTOGRAM aggregation parser.");
        }

        return lastAggBuilder;
    }


    //1. connect the groupby aggregations to each other
    //2.if there is a range/time aggregation connect to the last groupby aggregation
    //3. if there is no range/time aggregation connect the statistics aggregation to the last groupby aggregation
    private Aggregation handleGroupByAggregations(List<Aggregation.Builder> groupByAggs, Aggregation lastAgg) {
        //Duplicate the builder in order to reuse it
        Aggregation.Builder firstGroupByAggBuilder = groupByAggs.getFirst();
        Aggregation.Builder lastGroupByAggBuilder  = groupByAggs.getLast();

        if (Objects.nonNull(lastAgg)) {
            // Statistics aggregations were already added at rangeAggregation / time interval aggregation creation
            lastGroupByAggBuilder.aggregations(lastAgg._kind().name(), lastAgg);
        }
        else {
            addStatisticsSubAggregations(lastGroupByAggBuilder);
        }

        Aggregation.Builder aggBuilder;
        int                 groupByAggSize = groupByAggs.size();

        for (int i = 0; i < groupByAggSize; ++i) {
            // Get by reverse order
            aggBuilder = groupByAggs.get((groupByAggSize - 1) - i);

            String aggIdentifier = aggBuilder.build()._kind().name();

            // Stack 'group by' parsers for every sub aggregation
            parsers.addFirst(getGroupByAggregationParser(aggIdentifier));
            LOGGER.debug(String.format("Attached GROUP BY aggregation parser [%s]", aggIdentifier));
        }

        return firstGroupByAggBuilder.build();
    }

    //todo tal oyar - should be done
    private Aggregation.Builder addStatisticsSubAggregations(Aggregation.Builder parentAggregation) {
        MetricStatisticEnum      statistic       = request.getStatistic();
        Boolean                  isLegalStat     = true;
        String                   statAggName     = "";
        Map<String, Aggregation> subAggregations = new HashMap<>();

        switch (statistic) {
            case MINIMUM: {
                MinAggregation minStat    = createMinStatAggregation();
                String         minAggName = minStat._aggregationKind().name();
                subAggregations.put(minAggName, minStat._toAggregation());
                statAggName = minAggName;
            }
            break;
            case MAXIMUM: {
                MaxAggregation maxStat    = createMaxStatAggregation();
                String         maxAggName = maxStat._aggregationKind().name();
                subAggregations.put(maxAggName, maxStat._toAggregation());
                statAggName = maxAggName;
            }
            break;
            case SUM: {
                SumAggregation sumStat    = createSumStatAggregation();
                String         sumAggName = sumStat._aggregationKind().name();
                subAggregations.put(sumAggName, sumStat._toAggregation());
                statAggName = sumAggName;
            }
            break;
            case COUNT: {
                ValueCountAggregation countStat         = createCountStatAggregation();
                String                valueCountAggName = countStat._aggregationKind().name();
                subAggregations.put(valueCountAggName, countStat._toAggregation());
                statAggName = valueCountAggName;
            }
            break;
            case AVERAGE: {
                AverageAggregation avgStat    = createAverageStatAggregation();
                String             avgAggName = avgStat._aggregationKind().name();
                subAggregations.put(avgAggName, avgStat._toAggregation());
                statAggName = avgAggName;
            }
            break;
            default: {
                isLegalStat = false;
                String format = "Statistics [%s] is not supported, cannot create aggregation based on statistic.";
                String errMsg = String.format(format, statistic);
                LOGGER.error(errMsg);
            }
            break;
        }

        if (isLegalStat) {
            parentAggregation.aggregations(subAggregations);
            parsers.addFirst(getStatsAggregationParser(statAggName));
            LOGGER.debug(String.format("Attached STATS aggregation parser [%s]", statAggName));
        }

        return parentAggregation;
    }
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
        LOGGER.debug("Search Response Total Hits: [{}]", totalHits);

        if (Objects.isNull(totalHits) || totalHits.value() == 0) {
            LOGGER.debug("No matches returned from elastic search, returning an empty response");
            return retVal;
        }

        // All aggregation values should be save to the result map - linked hash map is mandatory for keeping the order on range aggregations
        Map<String, ElasticMetricAggregations> byRefResultMap = new LinkedHashMap<>();

        // Create composite key to identify parser aggregations in the first iteration
        AggCompositeKey compositeKey = new AggCompositeKey();
        //todo tal oyar - Is this just for the head of the aggregation? meaning we won't proceed with the parsing?
        compositeKey.setTermKey(IMetricAggregationParser.ORIGIN_AGG);

        // Load first parser aggregation to parse
        Map<AggCompositeKey, Map<String, Aggregate>> nextAggsToParse = new HashMap<>();

        nextAggsToParse.put(compositeKey, searchResponse.aggregations());

        for (IMetricAggregationParser parser : parsers) {
            nextAggsToParse = parser.parse(nextAggsToParse, byRefResultMap);
        }

        List<ElasticMetricDimension> dimensionsToAppend = request.getDimensions();

        Boolean      rangeReq        = isRangeRequest(request);
        Boolean      isRangeOuterAgg = isRangeOuterAgg();
        List<String> groupByNames    = request.getGroupBy() != null ? request.getGroupBy() : new ArrayList<>();

        // Add request dimensions to result structure
        if (MapUtils.isNotEmpty(byRefResultMap)) {
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
                        if (BooleanUtils.isFalse(groupByNames.contains(dimensionName))) {
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

        if (CollectionUtils.isNotEmpty(parsers)) {
            String aggregationName = parsers.getFirst().getAggregationName();
            retVal = StringUtils.equalsIgnoreCase(aggregationName, Aggregate.Kind.Range.name());
        }

        return retVal;
    }

    private Boolean isRangeRequest(ElasticMetricStatisticsRequest request) {
        ElasticMetricRange range = request.getRange();
        return range != null && BooleanUtils.isFalse(range.hasPartialData());
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
