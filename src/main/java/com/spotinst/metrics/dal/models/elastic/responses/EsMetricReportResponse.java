package com.spotinst.metrics.dal.models.elastic.responses;

import com.spotinst.metrics.dal.models.elastic.EsMetricResultStatus;

public class EsMetricReportResponse {
    private EsMetricResultStatus status;

    public EsMetricReportResponse() {
    }

    public EsMetricResultStatus getStatus() {
        return status;
    }

    public void setStatus(EsMetricResultStatus status) {
        this.status = status;
    }
}
