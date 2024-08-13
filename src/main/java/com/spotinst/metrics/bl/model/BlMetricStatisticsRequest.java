package com.spotinst.metrics.bl.model;

import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;

import java.util.List;

public class BlMetricStatisticsRequest {
    private String                  accountId;
    private String                  namespace;
    private String                  metricName;
    private List<String>            groupBy;
    private List<BlMetricDimension> dimensions;
    private MetricsTimeIntervalEnum timeInterval;
    private MetricStatisticEnum     statistic;
    private BlMetricRange           range;
    private BlMetricDateRange       dateRange;

    public BlMetricStatisticsRequest() {}

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public List<BlMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<BlMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public BlMetricRange getRange() {
        return range;
    }

    public void setRange(BlMetricRange range) {
        this.range = range;
    }

    public BlMetricDateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(BlMetricDateRange dateRange) {
        this.dateRange = dateRange;
    }

    public MetricsTimeIntervalEnum getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(MetricsTimeIntervalEnum timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public MetricStatisticEnum getStatistic() {
        return statistic;
    }

    public void setStatistic(MetricStatisticEnum statistic) {
        this.statistic = statistic;
    }

    @Override
    public String toString() {
        return "BlMetricStatisticsRequest{" + "accountId='" + accountId + '\'' + ", namespace='" + namespace + '\'' +
               ", metricName='" + metricName + '\'' + ", groupBy=" + groupBy + ", dimensions=" + dimensions +
               ", timeInterval=" + timeInterval + ", statistic=" + statistic + ", range=" + range + ", dateRange=" +
               dateRange + '}';
    }
}
