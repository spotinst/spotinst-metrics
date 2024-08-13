package com.spotinst.metrics.bl.model;

public class BlMetricDatapoint {
    private String             timestamp;
    private BlMetricStatistics statistics;

    public BlMetricDatapoint() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public BlMetricStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(BlMetricStatistics statistics) {
        this.statistics = statistics;
    }
}
