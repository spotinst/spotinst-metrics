package com.spotinst.metrics.bl.index.spotinst;

import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spotinst.metrics.bl.parsers.*;
import com.spotinst.metrics.commons.constants.MetricsConstants;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.ACCOUNT_ID_DIMENSION_NAME;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_STATISTIC_FIELD_PATH_FORMAT;

public class AggregationIndexManager extends BaseIndexManager2<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationIndexManager.class);

    public AggregationIndexManager(ElasticMetricStatisticsRequest request) {
        super(request);
    }

    // region Filters
    @Override
    protected BoolQuery createDataOwnershipQuery() {
        BoolQuery retVal;

        String accountId = request.getAccountId();

        Query accountIdQuery =
                QueryBuilders.term().field(ACCOUNT_ID_DIMENSION_NAME).value(accountId).build()._toQuery();

        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }
    // endregion

    // region Statistics
    protected SumAggregation createSumStatAggregation() {
        SumAggregation retVal;

        String fieldPath = getMetricQueryFieldPath();
        retVal = AggregationBuilders.sum().field(fieldPath).build();

        return retVal;
    }

    @Override
    protected String getMetricQueryFieldPath() {
        String              metricName = request.getMetricName();
        MetricStatisticEnum statistic  = request.getStatistic();
        // Aggregation document uses shorten names for statistics e.g. avg, min, max etc..
        String retVal = String.format(METRIC_STATISTIC_FIELD_PATH_FORMAT, metricName, statistic.getShortenValue());
        return retVal;
    }

    @Override
    protected String getMetricExistsFieldPath() {
        String metricName = request.getMetricName();

        // Metric existence on aggregated index should always refer to the 'count' field
        String retVal = String.format(METRIC_STATISTIC_FIELD_PATH_FORMAT, metricName,
                                      MetricStatisticEnum.COUNT.getShortenValue());

        return retVal;
    }

    @Override
    protected String getDimensionQueryFieldPath(String dimension) {
        return String.format(MetricsConstants.ElasticMetricConstants.DIMENSION_FIELD_PATH_FORMAT, dimension);
    }
    // endregion

    // region Parse Response
    @Override
    protected BaseMetricAggregationParser getStatsAggregationParser(String identifier) {
        return new MetricAggStatsAggregationParser(identifier, request);
    }

    @Override
    protected BaseMetricAggregationParser getRangeAggregationParser(String identifier) {
        return new MetricRangeAggregationParser(identifier, request);
    }

    @Override
    protected BaseMetricAggregationParser getTimeIntervalAggregationParser(String identifier) {
        return new MetricDateHistogramAggregationParser(identifier, request);
    }

    @Override
    protected BaseMetricAggregationParser getGroupByAggregationParser(String identifier) {
        return new MetricGroupByAggregationParser(identifier, request);
    }
    // endregion
}
