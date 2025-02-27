package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.parsers.*;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import org.apache.commons.lang3.StringUtils;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_SUM_NAME;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.ACCOUNT_ID_RAW_DIMENSION_NAME;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.DIMENSION_FIELD_PATH_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_KEYWORD_SUFFIX;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_VALUE_FIELD_PATH_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.MetricScripts.SUM_AGG_SCRIPT_FORMAT_RAW_INDEX;

public class RawIndexManager extends BaseIndexManager<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawIndexManager.class);

    // If service configured to read from optimized index, field paths should exclude the use of 'keyword'
    private static Boolean IS_READ_FROM_OPTIMIZED_INDEX;

    public RawIndexManager(ElasticMetricStatisticsRequest request) {
        super(request);

        ElasticConfig config = MetricsAppContext.getInstance()
                                                .getConfiguration()
                                                .getElastic();

        // By default, service shouldn't read from optimized index (may be changed later on)
        IS_READ_FROM_OPTIMIZED_INDEX = false;

        IndexNamePatterns indexNamePatterns = config.getIndexNamePatterns();

        if(indexNamePatterns != null) {
            String readPattern = indexNamePatterns.getReadPattern();

            if(readPattern != null) {
                String idxPrefix = StringUtils.isEmpty(readPattern) ? "" : readPattern;
//                IS_READ_FROM_OPTIMIZED_INDEX = idxPrefix.startsWith("opt_"); //TODO Tal: handle it later
            }
        }
    }

    // region Filters
//    @Override
//    protected BoolQuery createDataOwnershipQuery() {
//        String           accountId     = request.getAccountId();
////        BoolQuery retVal        = QueryBuilders.boolQuery();
//        BoolQuery.Builder retVal = new BoolQuery.Builder();
//        String           accountIdPath = ACCOUNT_ID_RAW_DIMENSION_NAME;
//
//        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
////        if(IS_READ_FROM_OPTIMIZED_INDEX == false) {
////            accountIdPath += METRIC_KEYWORD_SUFFIX;
////        }
//
////        TermQuery accountIdQuery = QueryBuilders.termQuery(accountIdPath, accountId);
//        TermQuery accountIdQuery = QueryBuilders.term().field(accountIdPath).value(accountId).build();
//
//        retVal.should(accountIdQuery);
//
//        return retVal;
//    }

    @Override
    protected BoolQuery createDataOwnershipQuery() {
        String accountId = request.getAccountId();
        BoolQuery retVal;
        String accountIdPath = ACCOUNT_ID_RAW_DIMENSION_NAME;

        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
        // if (!IS_READ_FROM_OPTIMIZED_INDEX) {
        //     accountIdPath += METRIC_KEYWORD_SUFFIX;
        // }

        TermQuery.Builder termQueryBuilder = new TermQuery.Builder();
        termQueryBuilder.field(accountIdPath);
        termQueryBuilder.value(accountId);
        TermQuery accountIdQuery = termQueryBuilder.build();

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        boolQueryBuilder.should(s -> s.term(accountIdQuery));
        retVal = boolQueryBuilder.build();

//        TermQuery accountIdTermQuery = QueryBuilders.term().field(accountIdPath).value(accountId).build();
//        Query accountIdQuery = new Query.Builder().term(accountIdTermQuery).build();

//        retVal.should(accountIdQuery);

        return retVal;
    }
    // endregion

    // region Statistics
    //TODO Tal It's working well
    protected Aggregation createSumStatAggregation() {
        Aggregation retVal;
        String                metricName = request.getMetricName();
        SumAggregation.Builder sumAggBuilder = AggregationBuilders.sum();

        String fieldNameStr = String.format(METRIC_VALUE_FIELD_PATH_FORMAT, metricName);

        sumAggBuilder.field(fieldNameStr);

        SumAggregation sumAgg = sumAggBuilder.build();

        retVal = sumAgg._toAggregation();

        return retVal;
    }

    @Override
    protected String getMetricQueryFieldPath() {
        String metricName      = request.getMetricName();
        String metricValuePath = String.format(METRIC_VALUE_FIELD_PATH_FORMAT, metricName);
        return metricValuePath;
    }

    @Override
    protected String getMetricExistsFieldPath() {
        return getMetricQueryFieldPath();
    }

    @Override
    protected String getDimensionQueryFieldPath(String dimension) {
        String dimensionPath = String.format(DIMENSION_FIELD_PATH_FORMAT, dimension);

        // If reading from a non-optimized index, we still need to append the .keyword suffix
//        if(IS_READ_FROM_OPTIMIZED_INDEX == false) {
//            dimensionPath += METRIC_KEYWORD_SUFFIX;
//        }
        return dimensionPath;
    }
    // endregion

    // region Parse Response
    @Override
    protected BaseMetricAggregationParser getStatsAggregationParser(String identifier) {
        return new MetricStatsAggregationParser(identifier, request);
    }

    @Override
    protected BaseMetricAggregationParser getRangeAggregationParser(String identifier) {
//        return new MetricRangeAggregationParser(identifier, request);
        return null;
    }

    @Override
    protected BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier) {
        return new MetricDateHistogramAggregationParser(identifier, request);
    }

    @Override
    protected BaseMetricAggregationParser getGroupByAggregationParser(String identifier) {
//        return new MetricGroupByAggregationParser(identifier, request);
        return null;
    }
    // endregion
}
