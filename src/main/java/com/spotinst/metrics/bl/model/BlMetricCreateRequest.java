package com.spotinst.metrics.bl.model;

import java.util.List;

public class BlMetricCreateRequest {
    private List<BlMetricDocument> metricDocuments;

    public BlMetricCreateRequest() {
    }

    public List<BlMetricDocument> getMetricDocuments() {
        return metricDocuments;
    }

    public void setMetricDocuments(List<BlMetricDocument> metricDocuments) {
        this.metricDocuments = metricDocuments;
    }
}
