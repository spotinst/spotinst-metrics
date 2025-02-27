//package com.spotinst.metrics.bl.index.spotinst;
//
//import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
//import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
//import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
//import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
//import com.spotinst.metrics.bl.parsers.*;
//import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
//import com.spotinst.metrics.commons.constants.MetricsConstants;
//import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_SUM_NAME;
//import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.ACCOUNT_ID_DIMENSION_NAME;
//import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_STATISTIC_FIELD_PATH_FORMAT;
//import static com.spotinst.metrics.commons.constants.MetricsConstants.Aggregations.AGG_METRIC_SUM_NAME;
//
///**
// * Created by zachi.nachshon on 4/24/17.
// */
//public class AggregationIndexManager extends BaseIndexManager<ElasticMetricStatisticsRequest> {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationIndexManager.class);
//
//    public AggregationIndexManager(EsMetricStatisticsRequest request) {
//        super(request);
//    }
//
//    // region Filters
//    @Override
//    protected BoolQuery createDataOwnershipQuery() {
//        String           accountId      = request.getAccountId();
//        BoolQuery retVal         = QueryBuilders.bool();
////        TermQuery.Builder accountIdQuery = QueryBuilders.term("account_id", accountId);
//        TermQuery accountIdQuery = TermQuery.of(t -> t.field("account_id").value(accountId));
//
////        retVal.should(accountIdQuery);
//        retVal.should(s -> s.term(accountIdQuery));
//
//        return retVal;
//    }
//    // endregion
//
//    // region Statistics
////    protected Aggregation.Builder createSumStatAggregation() {
////        String         fieldPath = getMetricQueryFieldPath();
////        SumAggregation.Builder retVal    = AggregationBuilders.sum(AGG_METRIC_SUM_NAME).field(fieldPath);
////        return retVal;
////    }
//    protected SumAggregation createSumStatAggregation() {
//        String fieldPath = getMetricQueryFieldPath();
//        SumAggregation retVal = AggregationBuilders.sum().field(fieldPath).build();
//        return retVal;
//    }
//
//    @Override
//    protected String getMetricQueryFieldPath() {
//        String              metricName = request.getMetricName();
//        MetricStatisticEnum statistic  = request.getStatistic();
//        // Aggregation document uses shorten names for statistics e.g. avg, min, max etc..
//        String retVal = String.format(METRIC_STATISTIC_FIELD_PATH_FORMAT, metricName, statistic.getShortenValue());
//        return retVal;
//    }
//
//    @Override
//    protected String getMetricExistsFieldPath() {
//        String metricName = request.getMetricName();
//
//        // Metric existence on aggregated index should always refer to the 'count' field
//        String retVal = String.format(METRIC_STATISTIC_FIELD_PATH_FORMAT,
//                                      metricName,
//                                      MetricStatisticEnum.COUNT.getShortenValue());
//
//        return retVal;
//    }
//
//    @Override
//    protected String getDimensionQueryFieldPath(String dimension) {
//        String dimensionPath = String.format(EsMetricConsts.DIMENSION_FIELD_PATH_FORMAT, dimension);
//        return dimensionPath;
//    }
//    // endregion
//
//    // region Parse Response
//    @Override
//    protected BaseMetricAggregationParser getStatsAggregationParser(String identifier) {
//        return new MetricAggStatsAggregationParser(identifier, request);
//    }
//
//    @Override
//    protected BaseMetricAggregationParser getRangeAggregationParser(String identifier) {
//        return new MetricRangeAggregationParser(identifier, request);
//    }
//
//    @Override
//    protected BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier) {
//        return new MetricDateHistogramAggregationParser(identifier, request);
//    }
//
//    @Override
//    protected BaseMetricAggregationParser getGroupByAggregationParser(String identifier) {
//        return new MetricGroupByAggregationParser(identifier, request);
//    }
//    // endregion
//}
