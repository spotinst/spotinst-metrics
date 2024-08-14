package com.spotinst.metrics.bl.cmds;

import com.spotinst.commons.errors.ErrorCodesCommon;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.repos.MetricsRepoManager;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportMetricCmd {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetricCmd.class);

    private final BlMetricReportRequest bulkCreateRequest;
    private final String                index;

    public ReportMetricCmd(BlMetricReportRequest bulkCreateRequest, String index) {
        this.bulkCreateRequest = bulkCreateRequest;
        this.index = index;
    }

    public void execute() {
        LOGGER.info("Reporting {} metrics", bulkCreateRequest.getMetricDocuments().size());

        RepoGenericResponse<EsMetricReportResponse> createResponse =
                MetricsRepoManager.metricRepo.report(bulkCreateRequest, index);

        if (createResponse.isRequestSucceed()) {
            LOGGER.info("Successfully reported {} metrics", bulkCreateRequest.getMetricDocuments().size());
        }
        else {
            LOGGER.error("Failed to report metrics. Errors: {}", createResponse.getDalErrors());
            throw new BlException(ErrorCodesCommon.GENERAL_ERROR, "Failed to report metrics");
        }
    }
}
