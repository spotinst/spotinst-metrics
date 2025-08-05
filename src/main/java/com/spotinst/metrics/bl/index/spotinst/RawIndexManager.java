package com.spotinst.metrics.bl.index.spotinst;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.parsers.*;
import com.spotinst.metrics.commons.configuration.ElasticIndex;
import com.spotinst.metrics.commons.enums.IndexSearchTypeEnum;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.*;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Namespace.*;

public class RawIndexManager {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatsAggregationParser.class);

    protected ElasticMetricStatisticsRequest request;
    protected List<IMetricAggregationParser> parsers;
    protected IndexSearchTypeEnum            searchType;
    //endregion

    //region Constructor
    public RawIndexManager(ElasticMetricStatisticsRequest request, String index) {
        this.request = request;
        this.parsers = new ArrayList<>();
        initializeIndexSearchType(index);
    }
    //endregion

    //region Initialize Search Type
    private void initializeIndexSearchType(String index) {
        if (Objects.nonNull(index)) {
            ElasticIndex matchingIndex =
                    MetricsAppContext.getInstance().getConfiguration().getElastic().getIndexes().stream()
                                     .filter(i -> Objects.equals(index, i.getName())).findFirst().orElse(null);

            if (Objects.nonNull(matchingIndex)) {
                this.searchType = IndexSearchTypeEnum.fromName(matchingIndex.getSearchType());
            }
        }
        else {
            this.searchType = IndexSearchTypeEnum.EXACT_MATCH;
        }
    }
    //endregion

    // region Filters
    public void setFilters(SearchRequest.Builder searchRequestBuilder) throws DalException {
        TermsQuery       namespaceQuery         = createNamespaceQuery();
        List<TermsQuery> dimensionQueries       = createDimensionsQueries();
        ExistsQuery      metricFieldExistsQuery = createMetricExistsQuery();
        RangeQuery       timeQuery              = createTimeQuery();
        BoolQuery        dataOwnershipQuery     = createDataOwnershipQuery();

        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        boolQueryBuilder.must(a -> a.exists(metricFieldExistsQuery));
        boolQueryBuilder.filter(f -> f.terms(namespaceQuery));
        boolQueryBuilder.filter(b -> b.bool(dataOwnershipQuery));
        boolQueryBuilder.filter(f -> f.range(timeQuery));

        for (TermsQuery tQuery : dimensionQueries) {
            boolQueryBuilder.filter(f -> f.terms(tQuery));
        }

        searchRequestBuilder.query(f -> f.bool(boolQueryBuilder.build()));

        searchRequestBuilder.requestCache(true);

        // Do not return documents, we care about the aggregation content
        searchRequestBuilder.size(0);
    }

    private List<TermsQuery> createDimensionsQueries() throws DalException {
        List<TermsQuery>             retVal;
        List<ElasticMetricDimension> dimensions = request.getDimensions();

        if (CollectionUtils.isNotEmpty(dimensions)) {
            retVal = new ArrayList<>();

            // Filters with the same key should have the OR operator between them.
            // Example: multiple dimensions with different values e.g target_id with id-123 and id-234
            Map<String, List<Object>> dimensionQueriesByPath = new HashMap<>();

            dimensions.forEach(d -> {
                String path           = getDimensionQueryFieldPath(d.getName());
                String dimensionValue = d.getValue();

                // If we've already have a query with the same path - append, else add
                if (dimensionQueriesByPath.containsKey(path)) {
                    List<Object> existingDimensionValues = dimensionQueriesByPath.get(path);
                    existingDimensionValues.add(dimensionValue);
                }
                else {
                    List<Object> newDimensionValues = new ArrayList<>();
                    newDimensionValues.add(dimensionValue);
                    dimensionQueriesByPath.put(path, newDimensionValues);
                }
            });

            for (Map.Entry<String, List<Object>> entry : dimensionQueriesByPath.entrySet()) {
                String             dimensionPath     = entry.getKey();
                List<FieldValue>   dimensionValues   = entry.getValue().stream().map(FieldValue::of).toList();
                TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
                termsQueryBuilder.field(dimensionPath);
                termsQueryBuilder.terms(t -> t.value(dimensionValues));
                TermsQuery termsQuery = termsQueryBuilder.build();
                retVal.add(termsQuery);
            }

            LOGGER.debug("Created terms queries for [{}] dimensions", dimensionQueriesByPath.size());
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

        //todo tal - should check search type also here?
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

        String path = getMetricExistsFieldPath();

        retVal = QueryBuilders.exists().field(path).build();

        LOGGER.debug("Created exists query on metric [{}]", metricName);

        return retVal;
    }

    private RangeQuery createTimeQuery() {
        RangeQuery.Builder retVal = QueryBuilders.range();

        ElasticMetricDateRange dateRange = request.getDateRange();

        if (dateRange != null && dateRange.getFrom() != null && dateRange.getTo() != null) {
            LOGGER.debug("Starting to create query based on date range...");

            Date fromDate = dateRange.getFrom();
            Date toDate   = dateRange.getTo();

            String fromDateFormatted = ElasticMetricTimeUtils.dateToString(fromDate);
            String toDateFormatted   = ElasticMetricTimeUtils.dateToString(toDate);

            retVal.date(a -> a.field(TIMESTAMP_FIELD_PATH).gte(fromDateFormatted).lte(toDateFormatted));

            LOGGER.debug(
                    String.format("Finished creating query based on date range, from [%s] to [%s]", fromDate, toDate));
        }
        else {
            LOGGER.debug("Cannot create query, missing [dateRange] values");
        }

        return retVal.build();
    }

    protected BoolQuery createDataOwnershipQuery() {
        BoolQuery retVal;
        String    accountId          = request.getAccountId();
        String    accountIdFieldPath = ACCOUNT_ID_RAW_DIMENSION_NAME;

        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            accountIdFieldPath += METRIC_KEYWORD_SUFFIX;
        }

        Query accountIdQuery = QueryBuilders.term().field(accountIdFieldPath).value(accountId).build()._toQuery();
        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }
    // endregion

    // region Statistics
    private AverageAggregation createAverageStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.avg().field(fieldPath).build();
    }

    private MinAggregation createMinStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.min().field(fieldPath).build();
    }

    private MaxAggregation createMaxStatAggregation() {
        String fieldPath = getMetricQueryFieldPath();
        return AggregationBuilders.max().field(fieldPath).build();
    }

    private ValueCountAggregation createCountStatAggregation() {
        ValueCountAggregation retVal;
        String                metricName = request.getMetricName();

        String field = String.format(METRIC_COUNT_FIELD_PATH_FORMAT, metricName);

        retVal = AggregationBuilders.valueCount().field(field).build();

        return retVal;
    }

    protected SumAggregation createSumStatAggregation() {
        SumAggregation retVal;
        String         metricName = request.getMetricName();

        String field = String.format(METRIC_VALUE_FIELD_PATH_FORMAT, metricName);
        retVal = AggregationBuilders.sum().field(field).build();

        return retVal;
    }

    protected String getMetricQueryFieldPath() {
        return String.format(METRIC_VALUE_FIELD_PATH_FORMAT, request.getMetricName());
    }

    protected String getMetricExistsFieldPath() {
        return getMetricQueryFieldPath();
    }

    protected String getDimensionQueryFieldPath(String dimension) {
        String dimensionPath = String.format(DIMENSION_FIELD_PATH_FORMAT, dimension);

        // If the search type is not EXACT_MATCH, append the .keyword suffix to the dimension path
        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            dimensionPath += METRIC_KEYWORD_SUFFIX;
        }

        return dimensionPath;
    }
    // endregion

    // region Aggregations
    public void setAggregations(SearchRequest.Builder searchRequestBuilder) throws DalException, IOException {
        // Create range aggregation
        ElasticMetricRange      range             = request.getRange();
        MetricsTimeIntervalEnum timeInterval      = request.getTimeInterval();
        List<String>            groupByDimensions = request.getGroupBy();

        Map<String, Aggregation.Builder> finalAggBuilderByName;

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
            //todo tal - can i use the addstatisticsSubAggregations method here?
            //In case no range/time interval and no groupBy aggregations, build a simple statistic aggregation query
            finalAggBuilderByName = createStatisticsAggregations();
        }

        Map.Entry<String, Aggregation.Builder> aggBuilderByNameEntry =
                finalAggBuilderByName.entrySet().stream().findFirst().orElse(null);

        if (Objects.nonNull(aggBuilderByNameEntry)) {
            searchRequestBuilder.aggregations(aggBuilderByNameEntry.getKey(), aggBuilderByNameEntry.getValue().build());
        }
    }

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
        Map<String, Aggregation.Builder> retVal          = new HashMap<>();
        Aggregation.Builder              rangeAggBuilder = new Aggregation.Builder();
        ;
        ElasticMetricRange range = request.getRange();

        LOGGER.debug("Starting to create range aggregation.");

        String metricQueryFieldPath = getMetricQueryFieldPath();

        Double minimum = range.getMinimum();
        Double maximum = range.getMaximum();
        Double gap     = range.getGap();

        List<AggregationRange> ranges = new LinkedList<>();

        Double from = roundFix(minimum, 10);
        Double to;

        for (Double i = from; i <= maximum; ) {
            to = roundFix(i + gap, 10);
            ranges.add(new AggregationRange.Builder().from(i).to(to).build());
            i = to;
        }

        rangeAggBuilder.range(r -> r.field(metricQueryFieldPath).ranges(ranges));

        addStatisticsSubAggregations(rangeAggBuilder);

        BaseMetricAggregationParser rangeAggregationParser = getRangeAggregationParser(Aggregation.Kind.Range.name());
        parsers.addFirst(rangeAggregationParser);

        //Add GroupBy Aggregations
        List<String> groupByDimensions = request.getGroupBy();

        if (CollectionUtils.isNotEmpty(groupByDimensions)) {
            retVal = createGroupByAggregations(rangeAggBuilder);
        }
        else {
            retVal.put(Aggregation.Kind.Range.name(), rangeAggBuilder);
        }

        LOGGER.debug("Finished creating range aggregation for value [{}]: minimum [{}] maximum [{}] with a gap of [{}]",
                     metricQueryFieldPath, minimum, maximum, gap);

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

        LOGGER.debug("Starting to create [groupBy] aggregation.");

        Aggregation.Builder lastAggBuilder = null;
        String              aggName;
        int                 groupBySize    = groupByDimensions.size();
        int                 counter        = 0;

        for (String groupByDimension : groupByDimensions) {
            counter++;

            String dimensionPath = getDimensionQueryFieldPath(groupByDimension);
            aggName = String.format(AGG_GROUP_BY_DIMENSION_FORMAT, groupByDimension);

            Aggregation.Builder groupByAggBuilder = new Aggregation.Builder();
            groupByAggBuilder.terms(t -> t.field(dimensionPath).minDocCount(1)).aggregations(new HashMap<>());
            groupByOrder.add(aggName);

            if (Objects.equals(counter, groupBySize)) {
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

    private void addStatisticsSubAggregations(Aggregation.Builder parentAggregation) {
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
            BaseMetricAggregationParser statsAggregationParser = getStatsAggregationParser(statAggName);
            parsers.addFirst(statsAggregationParser);

            LOGGER.debug(String.format("Attached STATS aggregation parser [%s].", statAggName));
        }
    }

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
    // endregion

    // region Parsers
    public ElasticMetricStatisticsResponse parseResponse(
            SearchResponse<ElasticMetricStatisticsResponse> searchResponse) throws DalException {
        ElasticMetricStatisticsResponse retVal = new ElasticMetricStatisticsResponse();

        // All aggregation values should be saved to the result map - linked hash map is mandatory for keeping the order on range aggregations
        Map<String, ElasticMetricAggregations> resultMap = new LinkedHashMap<>();

        // Create composite key to identify parser aggregations in the first iteration
        AggCompositeKey compositeKey = new AggCompositeKey();

        compositeKey.setTermKey(IMetricAggregationParser.ORIGIN_AGG);

        // Load first parser aggregation to parse
        Map<AggCompositeKey, Map<String, Aggregate>> nextAggsToParse = new HashMap<>();

        nextAggsToParse.put(compositeKey, searchResponse.aggregations());

        for (IMetricAggregationParser parser : parsers) {
            nextAggsToParse = parser.parse(nextAggsToParse, resultMap);
        }

        List<ElasticMetricDimension> dimensionsToAppend = request.getDimensions();

        Boolean      rangeReq        = isRangeRequest(request);
        Boolean      isRangeOuterAgg = isRangeOuterAgg();
        List<String> groupByNames    = request.getGroupBy() != null ? request.getGroupBy() : new ArrayList<>();

        // Add request dimensions to result structure

        for (ElasticMetricAggregations resultValue : resultMap.values()) {

            // Append dimensions only for aggregations:
            //  1. Not containing a ranged aggregation
            //  2. The outer aggregation is the ranged one
            if (BooleanUtils.isFalse(rangeReq) || isRangeOuterAgg) {
                dimensionsToAppend.forEach(d -> {
                    String dimensionName = d.getName();

                    // Add dimension only if it differ by name
                    if (BooleanUtils.isFalse(groupByNames.contains(dimensionName))) {
                        resultValue.addDimension(d);
                    }
                });
            }

            // Add aggregation to final result
            retVal.addMetricAggregation(resultValue);

        }

        return retVal;
    }

    protected BaseMetricAggregationParser getStatsAggregationParser(String identifier) {
        return new MetricStatsAggregationParser(identifier, request);
    }

    protected BaseMetricAggregationParser getRangeAggregationParser(String identifier) {
        return new MetricRangeAggregationParser(identifier, request);
    }

    protected BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier) {
        return new MetricDateHistogramAggregationParser(identifier, request);
    }

    protected BaseMetricAggregationParser getGroupByAggregationParser(String identifier) {
        return new MetricGroupByAggregationParser(identifier, request);
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
}
