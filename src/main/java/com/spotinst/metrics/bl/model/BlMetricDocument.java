package com.spotinst.metrics.bl.model;

import java.util.List;

public class BlMetricDocument {
    private String                  accountId;
    private String                  namespace;
    private String                  timestamp;
    private List<BlMetricDimension> dimensions;
    private List<BlMetric>          metrics;

    public BlMetricDocument() {
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

    public List<BlMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<BlMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<BlMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<BlMetric> metrics) {
        this.metrics = metrics;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
