package com.spotinst.metrics.dal.models.elastic;

public class ElasticMetricDatapoint {
    private String                  timestamp;
    private ElasticMetricStatistics statistics;

    public ElasticMetricDatapoint() {
    }

    public ElasticMetricDatapoint(ElasticMetricStatistics statistics) {
        this.statistics = statistics;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ElasticMetricStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ElasticMetricStatistics statistics) {
        this.statistics = statistics;
    }
}
