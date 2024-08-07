package com.spotinst.metrics.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.metrics.api.model.ApiMetricDocument;

import java.util.List;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricsCreateRequest {
    private List<ApiMetricDocument> metricDocuments;

    public ApiMetricsCreateRequest() {
    }

    public List<ApiMetricDocument> getMetricDocuments() {
        return metricDocuments;
    }

    public void setMetricDocuments(List<ApiMetricDocument> metricDocuments) {
        this.metricDocuments = metricDocuments;
    }
}