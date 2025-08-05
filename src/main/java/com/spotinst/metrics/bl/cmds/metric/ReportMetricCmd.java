package com.spotinst.metrics.bl.cmds.metric;

import com.spotinst.commons.errors.ErrorCodesCommon;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.metrics.bl.model.BlMetricDocument;
import com.spotinst.metrics.bl.model.request.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.response.BlMetricReportResponse;
import com.spotinst.metrics.bl.repos.RepoManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReportMetricCmd {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetricCmd.class);

    public BlMetricReportResponse execute(BlMetricReportRequest blRequest, String index) {
        BlMetricReportResponse retVal = null;

        LOGGER.debug("Start reporting metrics data...");

        List<BlMetricDocument> metricDocuments = blRequest.getMetricDocuments();

        if (BooleanUtils.isFalse(CollectionUtils.isEmpty(metricDocuments))) {
            String accountId = RequestsContextManager.getContext().getAccountId();

            if (StringUtils.isEmpty(accountId)) {
                LOGGER.error(
                        "Cannot determine account id from request context, documents gets indexed w/out account id");
            }
            else {
                metricDocuments.forEach(doc -> doc.setAccountId(accountId));
            }

            RepoGenericResponse<BlMetricReportResponse> reportResponse =
                    RepoManager.metricRepo.report(blRequest, index);

            if (reportResponse.isRequestSucceed() && reportResponse.getValue() != null) {
                retVal = reportResponse.getValue();

                LOGGER.debug("Finished reporting metrics data");
            }
            else {
                String errorMsg = "Failed to report metrics";
                LOGGER.error("{}. Errors: {}", errorMsg, reportResponse.getDalErrors());
                throw new BlException(ErrorCodesCommon.GENERAL_ERROR, errorMsg);
            }
        }
        else {
            LOGGER.warn("No metrics were sent in the report metric request, aborting");
        }
        return retVal;
    }
}
