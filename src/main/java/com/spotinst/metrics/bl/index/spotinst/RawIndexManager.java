package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.parsers.*;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
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
                IS_READ_FROM_OPTIMIZED_INDEX = idxPrefix.startsWith("opt_");
            }
        }
    }

    // region Filters
    @Override
    protected BoolQueryBuilder createDataOwnershipQuery() {
        String           accountId     = request.getAccountId();
        BoolQueryBuilder retVal        = QueryBuilders.boolQuery();
        String           accountIdPath = ACCOUNT_ID_RAW_DIMENSION_NAME;

        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
        if(IS_READ_FROM_OPTIMIZED_INDEX == false) {
            accountIdPath += METRIC_KEYWORD_SUFFIX;
        }

        TermQueryBuilder accountIdQuery = QueryBuilders.termQuery(accountIdPath, accountId);

        retVal.should(accountIdQuery);

        return retVal;
    }
    // endregion

    // region Statistics
    protected AbstractAggregationBuilder createSumStatAggregation() {
        String                metricName = request.getMetricName();
        SumAggregationBuilder retVal     = AggregationBuilders.sum(AGG_METRIC_SUM_NAME);

        String sumScriptStr = String.format(SUM_AGG_SCRIPT_FORMAT_RAW_INDEX, metricName, metricName);

        Script sumScript = new Script(sumScriptStr);
        retVal.script(sumScript);

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
        if(IS_READ_FROM_OPTIMIZED_INDEX == false) {
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
