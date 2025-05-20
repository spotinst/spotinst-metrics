package com.spotinst.metrics.bl.index.spotinst;

import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.parsers.*;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.ACCOUNT_ID_RAW_DIMENSION_NAME;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.DIMENSION_FIELD_PATH_FORMAT;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_KEYWORD_SUFFIX;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_VALUE_FIELD_PATH_FORMAT;

public class RawIndexManager extends BaseIndexManager2<ElasticMetricStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawIndexManager.class);

    // If service configured to read from optimized index, field paths should exclude the use of 'keyword'
    private static Boolean IS_READ_FROM_OPTIMIZED_INDEX;

    public RawIndexManager(ElasticMetricStatisticsRequest request) {
        super(request);

        ElasticConfig config = MetricsAppContext.getInstance().getConfiguration().getElastic();

        // By default, service shouldn't read from optimized index (may be changed later on)
        IS_READ_FROM_OPTIMIZED_INDEX = false;

        IndexNamePatterns indexNamePatterns = config.getIndexNamePatterns();

        if (indexNamePatterns != null) {
            String readPattern = indexNamePatterns.getReadPattern();

            if (readPattern != null) {
                String idxPrefix = StringUtils.isEmpty(readPattern) ? "" : readPattern;
                IS_READ_FROM_OPTIMIZED_INDEX = idxPrefix.startsWith("opt_");
            }
        }
    }

    // region Filters
    @Override
    protected BoolQuery createDataOwnershipQuery() {
        BoolQuery retVal;

        String accountId     = request.getAccountId();
        String accountIdPath = ACCOUNT_ID_RAW_DIMENSION_NAME;

        //todo tal - probably need to always add the keyword suffix from now on
        // it was only added when reading from non-optimized index - but seems like it should be added always now.

        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
        if (IS_READ_FROM_OPTIMIZED_INDEX) {
            accountIdPath += METRIC_KEYWORD_SUFFIX;
        }

        Query accountIdQuery = QueryBuilders.term().field(accountIdPath).value(accountId).build()._toQuery();

        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }
    // endregion

    // region Statistics
    protected SumAggregation createSumStatAggregation() {
        SumAggregation retVal;
        String         metricName = request.getMetricName();

        String field = String.format(METRIC_VALUE_FIELD_PATH_FORMAT, metricName);
        retVal = AggregationBuilders.sum().field(field).build();

        return retVal;
    }

    @Override
    protected String getMetricQueryFieldPath() {
        return String.format(METRIC_VALUE_FIELD_PATH_FORMAT, request.getMetricName());
    }

    @Override
    protected String getMetricExistsFieldPath() {
        return getMetricQueryFieldPath();
    }

    @Override
    protected String getDimensionQueryFieldPath(String dimension) {
        String dimensionPath = String.format(DIMENSION_FIELD_PATH_FORMAT, dimension);

        //todo tal - probably need to always add the keyword suffix from now on
        // it was only added when reading from non-optimized index - but seems like it should be added always now.

        // If reading from a non-optimized index, we still need to append the .keyword suffix
        if (IS_READ_FROM_OPTIMIZED_INDEX) {
            dimensionPath += METRIC_KEYWORD_SUFFIX;
        }
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
