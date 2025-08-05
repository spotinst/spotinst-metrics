package com.spotinst.metrics.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.metrics.api.model.ApiMetricDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricsReportRequest {
    private List<ApiMetricDocument> metricDocuments;

    public ApiMetricsReportRequest() {
    }

}