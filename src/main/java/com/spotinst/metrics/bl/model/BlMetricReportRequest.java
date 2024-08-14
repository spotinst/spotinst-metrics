package com.spotinst.metrics.bl.model;

import java.util.List;

public class BlMetricReportRequest {
    private List<BlMetricDocument> metricDocuments;

    public BlMetricReportRequest() {
    }

    public List<BlMetricDocument> getMetricDocuments() {
        return metricDocuments;
    }

    public void setMetricDocuments(List<BlMetricDocument> metricDocuments) {
        this.metricDocuments = metricDocuments;
    }
}
