package com.spotinst.metrics.dal.models.elastic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.metrics.bl.model.BlMetric;
import com.spotinst.metrics.bl.model.BlMetricDimension;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticMetricDocument {
    private String                  accountId;
    private String                  namespace;
    private String                  timestamp;
    private List<BlMetricDimension> dimensions;
    private List<BlMetric>          metrics;

    public ElasticMetricDocument() {
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
