package com.spotinst.metrics.dal.models.elastic.requests;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;

import java.util.List;

/**
 * Created by Tal.Geva on 08/08/24.
 */
public class ElasticMetricReportRequest {
    private List<ElasticMetricDocument> metricDocuments;

    public ElasticMetricReportRequest() {
    }

    public List<ElasticMetricDocument> getMetricDocuments() {
        return metricDocuments;
    }

    public void setMetricDocuments(List<ElasticMetricDocument> metricDocuments) {
        this.metricDocuments = metricDocuments;
    }
}
