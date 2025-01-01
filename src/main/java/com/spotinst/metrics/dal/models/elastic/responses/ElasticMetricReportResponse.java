package com.spotinst.metrics.dal.models.elastic.responses;

import com.spotinst.metrics.bl.model.response.BlMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricResultStatus;

public class ElasticMetricReportResponse extends BlMetricReportResponse {
    private ElasticMetricResultStatus status;

    public ElasticMetricReportResponse() {
    }

//    public ElasticMetricResultStatus getStatus() {
//        return status;
//    }

    public void setStatus(ElasticMetricResultStatus status) {
        this.status = status;
    }
}
