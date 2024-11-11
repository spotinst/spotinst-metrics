package com.spotinst.metrics.dal.models.elastic.requests;

import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricRange;

import java.util.List;

public class ElasticMetricStatisticsRequest {
    private String                       accountId;
    private String                       namespace;
    private String                       metricName;
    private List<String>                 groupBy;
    private List<ElasticMetricDimension> dimensions;
    private MetricsTimeIntervalEnum      timeInterval;
    private MetricStatisticEnum          statistic;
    private ElasticMetricRange           range;
    private ElasticMetricDateRange       dateRange;

    public ElasticMetricStatisticsRequest() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

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

    public List<ElasticMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ElasticMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public MetricsTimeIntervalEnum getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(MetricsTimeIntervalEnum timeInterval) {
        this.timeInterval = timeInterval;
    }

    public MetricStatisticEnum getStatistic() {
        return statistic;
    }

    public void setStatistic(MetricStatisticEnum statistic) {
        this.statistic = statistic;
    }

    public ElasticMetricRange getRange() {
        return range;
    }

    public void setRange(ElasticMetricRange range) {
        this.range = range;
    }

    public ElasticMetricDateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(ElasticMetricDateRange dateRange) {
        this.dateRange = dateRange;
    }

    @Override
    public String toString() {
        return "ElasticMetricStatisticsRequest{" + "accountId='" + accountId + '\'' + ", namespace='" + namespace + '\'' +
               ", metricName='" + metricName + '\'' + ", groupBy=" + groupBy + ", dimensions=" + dimensions +
               ", timeInterval=" + timeInterval + ", statistic=" + statistic + ", range=" + range + ", dateRange=" +
               dateRange + '}';
    }
}