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

            if (reportResponse.isRequestSucceed() || reportResponse.getValue() != null) {
                retVal = reportResponse.getValue();

                LOGGER.debug("Finished reporting metrics data");

                //todo tal oyar - no need for it right?

                // Async update metric metadata cache after metrics handed over to the ES bulk processor
                // Must be after indexing phase in order to avoid java.util.ConcurrentModificationException due to
                // iteration on same documents from within the update metadata & report metrics flows
                //     updateMetricMetadataCache(accountId, blRequest);
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

    //todo tal oyar VERIFY - seems like no need for this. the cache is only used for lb-core.

    //        private void updateMetricMetadataCache(String accountId, BlMetricReportRequest request) {
    //            UpdateMetricMetadataCacheRunnable runnable = new UpdateMetricMetadataCacheRunnable(accountId, request);
    //            UpdateMetricMetadataCacheExecutor.getInstance().execute(runnable);
    //        }
}
