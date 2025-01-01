package com.spotinst.metrics.bl.cmds.metric;

import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.errors.ErrorCodes;
import com.spotinst.metrics.bl.model.BlMetricDateRange;
import com.spotinst.metrics.bl.model.request.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.response.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.repos.RepoManager;
import com.spotinst.metrics.commons.utils.ContextUtils;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.commons.utils.ElasticTimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GetMetricsStatisticsCmd extends BaseCmd<BlMetricStatisticsResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetMetricsStatisticsCmd.class);

    private static Integer QUERY_TIME_LIMIT_IN_DAYS;

    public GetMetricsStatisticsCmd() {
        super(null, null);

        QUERY_TIME_LIMIT_IN_DAYS =
                MetricsAppContext.getInstance().getConfiguration().getQueryConfig().getQueryTimeLimitInDays();
    }

    public BlMetricStatisticsResponse execute(BlMetricStatisticsRequest request, String index) {

        LOGGER.debug("Start filtering metric statistics...");

        // Check that the time range in the get statistics request is legit
        validateRequestTimeLimitations(request);

        // Set account id on metric stats request to enforce data ownership
        ContextUtils.setCtxAccountOnRequest(request);

        // Data ownership is being enforced by placing the account id from the context on the elastic search query
        BlMetricStatisticsResponse retVal;
        RepoGenericResponse<BlMetricStatisticsResponse> response =
                RepoManager.metricRepo.getMetricsStatistics(request, index);

        if (response.isRequestSucceed() == false || response.getValue() == null) {
            LOGGER.error(String.format("Failed to get metric statistics: %s", request.toString()),
                         response.getDalErrors());
            throw new BlException(ErrorCodes.FAILED_TO_GET_METRICS_STATISTICS, "Failed to get metric statistics");
        }

        LOGGER.debug("Finished filtering metric statistics");

        retVal = response.getValue();
        return retVal;
    }

    private void validateRequestTimeLimitations(BlMetricStatisticsRequest blMetStatsRequest) {
        BlMetricDateRange dateRange = blMetStatsRequest.getDateRange();

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
}
