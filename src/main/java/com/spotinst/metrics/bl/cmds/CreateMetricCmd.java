package com.spotinst.metrics.bl.cmds;

import com.spotinst.commons.errors.ErrorCodesCommon;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.repos.MetricsRepoManager;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateMetricCmd {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateMetricCmd.class);

    private final BlMetricCreateRequest bulkCreateRequest;

    public CreateMetricCmd(BlMetricCreateRequest bulkCreateRequest) {
        this.bulkCreateRequest = bulkCreateRequest;
    }

    public void execute() {
        LOGGER.info("Creating {} metrics", bulkCreateRequest.getMetricDocuments().size());

        RepoGenericResponse<EsMetricReportResponse> createResponse = MetricsRepoManager.metricRepo.create(bulkCreateRequest);

        if (createResponse.isRequestSucceed()) {
            LOGGER.info("Successfully created {} metrics", bulkCreateRequest.getMetricDocuments().size());
        }
        else {
            LOGGER.error("Failed to create metrics. Errors: {}", createResponse.getDalErrors());
            throw new BlException(ErrorCodesCommon.GENERAL_ERROR, "Failed to create metrics");
        }
    }
}
