package com.spotinst.metrics.dal.services.elastic.metric;


import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.errors.ErrorCodes;
import com.spotinst.metrics.commons.configuration.ElasticIndex;
import com.spotinst.metrics.commons.enums.IndexSearchTypeEnum;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.commons.utils.ElasticTimeUnit;
import com.spotinst.metrics.commons.utils.EsIndexNamingUtils;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticPercentileAggregation;
import com.spotinst.metrics.dal.models.elastic.ElasticPercentileAggregationResult;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticPercentilesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticPercentilesResponse;
import com.spotinst.metrics.dal.services.elastic.IElasticSearchPercentilesService;
import com.spotinst.metrics.dal.services.elastic.infra.BaseElasticSearchService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static co.elastic.clients.elasticsearch._types.ExpandWildcard.Open;
import static co.elastic.clients.elasticsearch._types.SearchType.QueryThenFetch;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.NAMESPACE_FIELD_PATH;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.TIMESTAMP_FIELD_PATH;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.*;

public class ElasticSearchPercentilesService extends BaseElasticSearchService implements IElasticSearchPercentilesService {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPercentilesService.class);

    public static final  String  METRIC_TO_FIELD_FORMAT  = "metrics.%s.value";
    private static final String  COUNT_AGG_NAME_TEMPLATE = "%s_count";
    private static final String  LAST_REPORT_AGG_NAME    = "last_report";
    private static       Integer QUERY_TIME_LIMIT_IN_DAYS;
    //endregion

    //region Constructors
    public ElasticSearchPercentilesService() {
        super();

        QUERY_TIME_LIMIT_IN_DAYS =
                MetricsAppContext.getInstance().getConfiguration().getQueryConfig().getQueryTimeLimitInDays();
    }
    //endregion

    //region Public Methods
    @Override
    public ElasticPercentilesResponse getPercentiles(ElasticPercentilesRequest request,
                                                     String index) throws DalException {
        ElasticPercentilesResponse retVal;
        StopWatch                  totalStopWatch = new StopWatch();
        totalStopWatch.start();
        validateRequest(request);

        BoolQuery.Builder                  boolQueryBuilder = QueryBuilders.bool();
        List<ElasticPercentileAggregation> percentiles      = request.getPercentiles();

        // add filters
        ElasticMetricDateRange       dateRange  = request.getDateRange();
        List<ElasticMetricDimension> dimensions = request.getDimensions();
        addFilters(index, boolQueryBuilder, dateRange, request.getAccountId(), request.getNamespace(), dimensions);

        // filter out zeroes (to keep existing behaviour)
        Boolean filterZeroes = request.getFilterZeroes();

        if (BooleanUtils.isNotFalse(filterZeroes)) {
            List<String> percentilesFieldNames = percentiles.stream().map(ElasticPercentileAggregation::getMetricName)
                                                            .map(this::convertMetricNameToFieldName)
                                                            .collect(Collectors.toList());
            addFilterOutZeroes(boolQueryBuilder, percentilesFieldNames);
            LOGGER.debug("Added filter out zeroes.");
        }
        else {
            LOGGER.debug("filterZeroes={}. Not filtering out zeroes.", filterZeroes);
        }

        List<String> indices = EsIndexNamingUtils.generateIndicesByDateRange(dateRange, index);

        // prepare the query
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
        searchRequestBuilder.index(indices).ignoreUnavailable(true).allowNoIndices(true).expandWildcards(Open)
                            .searchType(QueryThenFetch).explain(false).size(0);
        searchRequestBuilder.query(boolQueryBuilder.build());

        // add aggregations
        addAggregations(searchRequestBuilder, percentiles);

        // execute query
        try {
            SearchResponse<ElasticPercentilesResponse> searchResponse =
                    elasticsearchClient.search(searchRequestBuilder.build(), ElasticPercentilesResponse.class);
            retVal = parseResponse(searchResponse, percentiles);

            totalStopWatch.stop();

            LOGGER.info("Percentiles request ES took: [{}], total took [{}] ms", searchResponse.took(),
                        totalStopWatch.getTime());

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return retVal;
    }
    //endregion

    //region Private Methods
    //region Validations
    private void validateRequest(ElasticPercentilesRequest request) {
        validatePercentilesAggNames(request.getPercentiles());
        validateDateRange(request.getDateRange());
    }

    private void validateDateRange(ElasticMetricDateRange dateRange) {
        Date from = dateRange.getFrom();
        Date to   = dateRange.getTo();

        if (from.after(to)) {
            String message = "fromDate cannot be after toDate.";
            LOGGER.error(message);
            throw new BlException(ErrorCodes.FAILED_TO_GET_METRICS_STATISTICS, message);
        }

        validateRequestTimeLimitations(dateRange);
    }

    private void validateRequestTimeLimitations(ElasticMetricDateRange dateRange) {
        // Enforce time boundaries limitation to prevent over stressing elastic search when searching in all indexes
        if (dateRange == null || dateRange.getFrom() == null || dateRange.getTo() == null) {
            String errMsg = "Cannot get statistics without time boundaries, must supply [dateRange]";
            LOGGER.error(errMsg);
            throw new BlException(ErrorCodes.FAILED_TO_GET_METRICS_STATISTICS, errMsg);
        }

        // ElasticSearch contains up to 14 indices which represents the last 14 days
        // If request time exceeded the query time limit threshold, we'll be limiting the query time frame
        Long daysToQuery = ElasticMetricTimeUtils.getDiffByTimeUnitFromNow(dateRange.getFrom(), TimeUnit.DAYS);

        if (daysToQuery != null && daysToQuery > QUERY_TIME_LIMIT_IN_DAYS) {
            String errFormat = "Query request exceeds time boundaries limit of %s days, querying on last %s days";
            String errMsg    = String.format(errFormat, QUERY_TIME_LIMIT_IN_DAYS, QUERY_TIME_LIMIT_IN_DAYS);
            LOGGER.warn(errMsg);

            ElasticTimeUnit newOffset =
                    ElasticMetricTimeUtils.createOffsetTimeUnitFromNow(TimeUnit.DAYS, QUERY_TIME_LIMIT_IN_DAYS);

            Date newFromDate = newOffset.getFromDate();
            Date newToDate   = newOffset.getToDate();
            dateRange.setFrom(newFromDate);
            dateRange.setTo(newToDate);
        }
    }

    private void validatePercentilesAggNames(List<ElasticPercentileAggregation> requestPercentiles) {
        Set<String> aggNames =
                requestPercentiles.stream().map(ElasticPercentileAggregation::getAggName).collect(Collectors.toSet());

        if (aggNames.size() < requestPercentiles.size()) {
            String message = "There are duplicate aggNames in the request.";
            LOGGER.error(message);
            throw new BlException(ErrorCodes.FAILED_TO_GET_METRICS_STATISTICS, message);
        }
    }
    //endregion

    //region Filters
    private void addFilters(String index, BoolQuery.Builder boolQueryBuilder, ElasticMetricDateRange dateRange,
                            String accountId, String namespace,
                            List<ElasticMetricDimension> dimensions) throws DalException {
        TermsQuery namespaceQuery     = createNamespaceQuery(namespace);
        BoolQuery  dataOwnershipQuery = createDataOwnershipQuery(accountId);
        RangeQuery rangeQuery         = createTimeQuery(dateRange);

        IndexSearchTypeEnum searchType = resolveSearchType(index);

        boolQueryBuilder.filter(namespaceQuery).filter(dataOwnershipQuery).filter(rangeQuery);

        dimensions.forEach(dimension -> {
            String dimensionQueryFieldPath = getDimensionQueryFieldPath(dimension.getName(), searchType);
            boolQueryBuilder.filter(
                    QueryBuilders.term(tq -> tq.field(dimensionQueryFieldPath).value(dimension.getValue())));
        });
    }

    private IndexSearchTypeEnum resolveSearchType(String index) {
        IndexSearchTypeEnum retVal = IndexSearchTypeEnum.EXACT_MATCH;

        ElasticIndex esIndex = MetricsAppContext.getInstance().getConfiguration().getElastic().getIndexes().stream()
                                                .filter(i -> Objects.equals(i.getName(), index)).findFirst()
                                                .orElse(null);

        if (Objects.nonNull(esIndex)) {
            retVal = IndexSearchTypeEnum.fromName(esIndex.getSearchType());
        }

        return retVal;
    }


    protected String getDimensionQueryFieldPath(String dimension, IndexSearchTypeEnum searchType) {
        String dimensionPath = String.format(DIMENSION_FIELD_PATH_FORMAT, dimension);

        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            dimensionPath += METRIC_KEYWORD_SUFFIX;
        }

        return dimensionPath;
    }

    private RangeQuery createTimeQuery(ElasticMetricDateRange dateRange) throws DalException {
        RangeQuery.Builder rangeQueryBuilder = QueryBuilders.range();

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

    protected BoolQuery createDataOwnershipQuery(String accountId) {
        BoolQuery retVal;

        Query accountIdQuery =
                QueryBuilders.term().field(ACCOUNT_ID_RAW_DIMENSION_NAME).value(accountId).build()._toQuery();

        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }

    protected TermsQuery createNamespaceQuery(String namespace) throws DalException {
        TermsQuery retVal;

        if (StringUtils.isEmpty(namespace)) {
            String errMsg = "[namespace] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        retVal = QueryBuilders.terms().field(NAMESPACE_FIELD_PATH).terms(termsBuilder -> termsBuilder.value(
                Collections.singletonList(FieldValue.of(namespace)))).build();

        return retVal;
    }

    private void addFilterOutZeroes(BoolQuery.Builder boolQuery, List<String> fields) {
        fields.forEach(field -> boolQuery.mustNot(QueryBuilders.term().field(field).value(0).build()));
    }
    //endregion

    //region Aggregations
    private void addAggregations(SearchRequest.Builder searchRequestBuilder,
                                 List<ElasticPercentileAggregation> percentiles) {
        Aggregation lastReportAgg = AggregationBuilders.max(agg -> agg.field(TIMESTAMP_FIELD_PATH));
        searchRequestBuilder.aggregations(LAST_REPORT_AGG_NAME, lastReportAgg);

        for (ElasticPercentileAggregation percentile : percentiles) {
            String                   metricName     = percentile.getMetricName();
            String                   fieldName      = convertMetricNameToFieldName(metricName);
            Map<String, Aggregation> aggregationMap = new HashMap<>();

            Aggregation percentilesAgg =
                    AggregationBuilders.percentiles(agg -> agg.field(fieldName).percents(percentile.getPercentile()));
            aggregationMap.put(percentile.getAggName(), percentilesAgg);

            Aggregation existFilter = AggregationBuilders.filter(agg -> agg.exists(e -> e.field(fieldName)));
            aggregationMap.put(String.format(COUNT_AGG_NAME_TEMPLATE, percentile.getAggName()), existFilter);

            searchRequestBuilder.aggregations(aggregationMap);
        }
    }
    //endregion

    //region Response Parsing
    private ElasticPercentilesResponse parseResponse(SearchResponse<ElasticPercentilesResponse> searchResponse,
                                                     List<ElasticPercentileAggregation> requestedPercentiles) {
        ElasticPercentilesResponse retVal = new ElasticPercentilesResponse();

        TotalHits totalHits = searchResponse.hits().total();

        if (Objects.nonNull(totalHits)) {
            long totalHitsValues = totalHits.value();
            retVal.setTotalHits((int) totalHitsValues);

            if (totalHitsValues > 0) {
                Map<String, Aggregate> aggregations = searchResponse.aggregations();

                MaxAggregate lastReport     = aggregations.get(LAST_REPORT_AGG_NAME).max();
                String       lastReportDate = lastReport.valueAsString();
                retVal.setLastReportDate(lastReportDate);

                List<ElasticPercentileAggregationResult> percentiles =
                        parsePercentilesResponse(requestedPercentiles, aggregations);
                retVal.setPercentiles(percentiles);
            }
            else {
                LOGGER.warn("There were 0 data points that match the filter");
            }
        }

        return retVal;
    }

    private List<ElasticPercentileAggregationResult> parsePercentilesResponse(
            List<ElasticPercentileAggregation> requestedPercentiles, Map<String, Aggregate> aggregations) {
        List<ElasticPercentileAggregationResult> retVal = new LinkedList<>();

        for (ElasticPercentileAggregation percentile : requestedPercentiles) {
            // parse percentile
            String    aggName       = percentile.getAggName();
            Aggregate percentileAgg = aggregations.get(aggName);

            Map<String, String> percentileKeyedMap = percentileAgg.tdigestPercentiles().values().keyed();
            String              valueStr           = percentileKeyedMap.get(percentile.getPercentile().toString());
            Double              value              = stringToDouble(valueStr);

            // parse count
            String          countName = String.format(COUNT_AGG_NAME_TEMPLATE, aggName);
            FilterAggregate countAgg  = aggregations.get(countName).filter();
            double          docCount  = countAgg.docCount();

            ElasticPercentileAggregationResult result =
                    new ElasticPercentileAggregationResult(aggName, value, (int) docCount);

            retVal.add(result);
        }

        return retVal;
    }

    private static Double stringToDouble(String valueStr) {
        Double retVal = null;

        try {
            retVal = Double.parseDouble(valueStr);
            System.out.println("Parsed value: " + retVal);
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }

        return retVal;
    }

    private String convertMetricNameToFieldName(String metricName) {
        return String.format(METRIC_TO_FIELD_FORMAT, metricName);
    }
    //endregion
    //endregion
}