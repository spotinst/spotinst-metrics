package com.spotinst.metrics.dal.services.elastic;

import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.errors.ErrorCodes;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.commons.utils.ElasticTimeUnit;
import com.spotinst.metrics.commons.utils.EsIndexNamingUtils;
import com.spotinst.metrics.dal.models.elastic.*;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticPercentilesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticPercentilesResponse;
import com.spotinst.metrics.dal.services.elastic.infra.BaseElasticSearchService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Percentiles;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ElasticSearchPercentilesService extends BaseElasticSearchService implements IElasticSearchPercentilesService {

    //region Members
    private static final Logger LOGGER                     =
            LoggerFactory.getLogger(ElasticSearchPercentilesService.class);
    public static final  String METRIC_TO_FIELD_FORMAT     = "metrics.%s.value";
    private static final String NOT_IMPLEMENTED_MSG_FORMAT =
            "Method '%s' Not implemented in ElasticSearchPercentilesService and shouldn't been called. Please Investigate.";

    private static       Integer QUERY_TIME_LIMIT_IN_DAYS;
    private static final String  COUNT_AGG_NAME_TEMPLATE      = "%s_count";
    private static final String  DEFAULT_DOCUMENT_TYPE        = "balancer";
    private static final String  TIMESTAMP_FIELD_NAME         = "timestamp";
    private static final String  NAMESPACE_FIELD_NAME         = "namespace";
    private static final String  ACCOUNT_ID_FIELD_NAME        = "accountId";
    private static final String  DIMENSIONS_FIELD_NAME_PREFIX = "dimensions.";
    private static final String  LAST_REPORT_AGG_NAME         = "last_report";
    //endregion

    //region Constructors
    public ElasticSearchPercentilesService() {
        super();

        QUERY_TIME_LIMIT_IN_DAYS =
                MetricsAppContext.getInstance().getConfiguration().getQueryConfig().getQueryTimeLimitInDays();
    }
    //endregion

    //region Public Methods
    public static IElasticSearchPercentilesService getInstance() {
        return new ElasticSearchPercentilesService();
    }

    @Override
    public ElasticPercentilesResponse getPercentiles(ElasticPercentilesRequest request, String index) throws DalException {
        ElasticPercentilesResponse retVal = null;
        try {
            StopWatch totalStopWatch = new StopWatch();
            totalStopWatch.start();
            validateRequest(request);

            BoolQueryBuilder                   boolQuery   = QueryBuilders.boolQuery();
            List<ElasticPercentileAggregation> percentiles = request.getPercentiles();

            // add filters
            ElasticMetricDateRange dateRange = request.getDateRange();
            addTopLevelFilters(boolQuery, dateRange, request.getAccountId(), request.getNamespace());

            List<ElasticMetricDimension> dimensions = request.getDimensions();
            addDimensionsFilter(boolQuery, dimensions);

            // filter out zeroes (to keep existing behaviour)
            Boolean filterZeroes = request.getFilterZeroes();

            if (BooleanUtils.isNotFalse(filterZeroes)) {
                List<String> percentilesFieldNames =
                        percentiles.stream().map(ElasticPercentileAggregation::getMetricName)
                                   .map(this::convertMetricNameToFieldName).collect(Collectors.toList());
                addFilterOutZeroes(boolQuery, percentilesFieldNames);
                LOGGER.debug("Added filter out zeroes.");
            }
            else {
                LOGGER.debug("filterZeroes={}. Not filtering out zeroes.", filterZeroes);
            }

            String[]       indices = EsIndexNamingUtils.generateIndicesByDateRange(dateRange, index);
            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);

            // prepare the query

            SearchRequest searchRequest = new SearchRequest(indices);
            searchRequest.indicesOptions(options);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.explain(false); // don't show scoring
            sourceBuilder.query(boolQuery); // apply specific container filter
            sourceBuilder.size(0);  // return only aggregations

            // Add the aggregation
            sourceBuilder.aggregation(AggregationBuilders.max(LAST_REPORT_AGG_NAME).field(TIMESTAMP_FIELD_NAME));

            // add aggregations
            addAggregations(sourceBuilder, percentiles);

            // Attach source builder to search request
            searchRequest.source(sourceBuilder);

//            searchRequest.source(sourceBuilder.query(QueryBuilders.matchAllQuery()).explain(false));


            // execute query
            SearchResponse searchResponse =
                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
            retVal = parseResponse(searchResponse, percentiles);

            totalStopWatch.stop();
            long totalMillis = totalStopWatch.lastTaskTime().millis();
            LOGGER.info("Percentiles request ES took: [{}], total took [{}] ms", searchResponse.getTook(), totalMillis);
        }
        catch (IOException ex) {
            String errFormat = "Failed getting percentiles from elastic search for request: %s";
            String errMsg    = String.format(errFormat, request.toString());
            LOGGER.error(errMsg, ex);
            throw new DalException(errMsg, ex);
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
    private void addTopLevelFilters(BoolQueryBuilder boolQuery, ElasticMetricDateRange dateRange, String accountId,
                                    String namespace) {
        boolQuery.filter(QueryBuilders.termQuery(NAMESPACE_FIELD_NAME, namespace));
        boolQuery.filter(QueryBuilders.termQuery(ACCOUNT_ID_FIELD_NAME, accountId));

        boolQuery.filter(QueryBuilders.rangeQuery(TIMESTAMP_FIELD_NAME).from(dateRange.getFrom().getTime())
                                      .to(dateRange.getTo().getTime()));
    }

    private void addFilterOutZeroes(BoolQueryBuilder boolQuery, List<String> fields) {
        for (String field : fields) {
            boolQuery.mustNot(QueryBuilders.termQuery(field, "0"));
        }
    }

    private void addDimensionsFilter(BoolQueryBuilder boolQuery, List<ElasticMetricDimension> dimensions) {
        dimensions.forEach(dimension -> boolQuery.filter(
                QueryBuilders.termQuery(DIMENSIONS_FIELD_NAME_PREFIX + dimension.getName(), dimension.getValue())));
    }
    //endregion

    //region Aggregations
    private void addAggregations(SearchSourceBuilder searchSourceBuilder,
                                 List<ElasticPercentileAggregation> percentiles) {
        for (ElasticPercentileAggregation percentile : percentiles) {
            String metricName = percentile.getMetricName();
            String fieldName  = convertMetricNameToFieldName(metricName);

            searchSourceBuilder.aggregation(AggregationBuilders.percentiles(percentile.getAggName()).field(fieldName)
                                                               .percentiles(percentile.getPercentile()));

            searchSourceBuilder.aggregation(
                    AggregationBuilders.filter(String.format(COUNT_AGG_NAME_TEMPLATE, percentile.getAggName()),
                                               QueryBuilders.existsQuery(fieldName)));
        }
    }
    //endregion

    private ElasticPercentilesResponse parseResponse(SearchResponse searchResponse,
                                                     List<ElasticPercentileAggregation> requestedPercentiles) {
        ElasticPercentilesResponse retVal = new ElasticPercentilesResponse();

        if (searchResponse.getFailedShards() > 0) {
            LOGGER.error(String.format("There were errors in the elasticsearch query: %s",
                                       (Object) searchResponse.getShardFailures()));
        }

        TotalHits totalHits = searchResponse.getHits().getTotalHits();
        retVal.setTotalHits((int) totalHits.value);

        if (totalHits != null && totalHits.value > 0) {
            Aggregations aggregations = searchResponse.getAggregations();

            Max    lastReport     = aggregations.get(LAST_REPORT_AGG_NAME);
            String lastReportDate = lastReport.getValueAsString();
            retVal.setLastReportDate(lastReportDate);

            List<ElasticPercentileAggregationResult> percentiles =
                    parsePercentilesResponse(requestedPercentiles, aggregations);
            retVal.setPercentiles(percentiles);
        }
        else {
            LOGGER.warn("There were 0 data points that match the filter");
        }

        return retVal;
    }

    private List<ElasticPercentileAggregationResult> parsePercentilesResponse(
            List<ElasticPercentileAggregation> requestedPercentiles, Aggregations aggregations) {
        List<ElasticPercentileAggregationResult> retVal = new LinkedList<>();

        for (ElasticPercentileAggregation percentile : requestedPercentiles) {
            // parse percentile
            String      aggName     = percentile.getAggName();
            Percentiles aggregation = aggregations.get(aggName);
            double      pValue      = aggregation.percentile(percentile.getPercentile());

            // parse count
            String countName = String.format(COUNT_AGG_NAME_TEMPLATE, aggName);
            Filter countAgg  = aggregations.get(countName);
            long   docCount  = countAgg.getDocCount();

            ElasticPercentileAggregationResult result =
                    new ElasticPercentileAggregationResult(aggName, pValue, (int) docCount);

            retVal.add(result);
        }

        return retVal;
    }

    private String convertMetricNameToFieldName(String metricName) {
        String retVal = String.format(METRIC_TO_FIELD_FORMAT, metricName);

        return retVal;
    }
    //endregion

    //region Override methods
    //    @Override
    //    public ElasticMetricStatisticsResponse getAggregatedMetricsStatistics(
    //            ElasticMetricStatisticsRequest esMetricStatisticsRequest) throws DalException {
    //        throw new NotImplementedException(String.format(NOT_IMPLEMENTED_MSG_FORMAT, "getAggregatedMetricsStatistics"));
    //    }

    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(
            ElasticMetricStatisticsRequest esMetricStatisticsRequest, String index) throws DalException {
        throw new NotImplementedException(String.format(NOT_IMPLEMENTED_MSG_FORMAT, "getMetricsStatistics"));
    }

    @Override
    public ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest esDimensionsValuesRequest,
                                                               String index) throws DalException {
        throw new NotImplementedException(String.format(NOT_IMPLEMENTED_MSG_FORMAT, "getDimensionsValues"));
    }

    @Override
    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
                                                     String index) throws DalException {
        throw new NotImplementedException(String.format(NOT_IMPLEMENTED_MSG_FORMAT, "reportMetrics"));
    }
    //endregion
}