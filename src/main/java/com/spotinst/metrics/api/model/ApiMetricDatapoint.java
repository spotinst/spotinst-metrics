package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricDatapoint {
    private String               timestamp;
    private ApiMetricStatistics statistics;

    public ApiMetricDatapoint() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ApiMetricStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ApiMetricStatistics statistics) {
        this.statistics = statistics;
    }
}
