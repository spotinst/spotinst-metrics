package com.spotinst.metrics.bl.model;

import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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

    @Override
    public String toString() {
        return "BlMetricStatisticsRequest{" + "accountId='" + accountId + '\'' + ", namespace='" + namespace + '\'' +
               ", metricName='" + metricName + '\'' + ", groupBy=" + groupBy + ", dimensions=" + dimensions +
               ", timeInterval=" + timeInterval + ", statistic=" + statistic + ", range=" + range + ", dateRange=" +
               dateRange + '}';
    }
}
