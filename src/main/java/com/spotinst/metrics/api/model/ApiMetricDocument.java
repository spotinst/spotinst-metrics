package com.spotinst.metrics.api.model;

import com.spotinst.commons.response.api.items.IServiceResponseItem;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

//TODO Tal: check if IServiceResponseItem necessary and what it means
public class ApiMetricDocument implements IServiceResponseItem {
    @NotNull
    private String namespace;

    @NotNull
    private String timestamp;

    @Size(min = 1)
    private List<ApiMetricDimension> dimensions;

    @Size(min = 1)
    private List<ApiMetric> metrics;

    public ApiMetricDocument() {
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<ApiMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ApiMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<ApiMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<ApiMetric> metrics) {
        this.metrics = metrics;
    }
}
