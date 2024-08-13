package com.spotinst.metrics.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.metrics.api.model.ApiMetricDateRange;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiMetricRange;
import io.dropwizard.validation.OneOf;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMetricStatisticsRequest {
    @NotNull
    private String namespace;

    @NotNull
    private String metricName;

    @Size(min = 1)
    private List<ApiMetricDimension> dimensions;

    @Valid
    @NotNull
    private ApiMetricDateRange dateRange;

    @OneOf(value = {"1m", "2m", "5m", "15m", "30m", "1h", "2h", "6h", "12h", "24h"})
    private String timeInterval;

    @NotNull
    @OneOf(value = {"average", "count", "minimum", "maximum", "sum"})
    private String statistic;

    private List<String>   groupBy;
    private ApiMetricRange range;

    public ApiMetricStatisticsRequest() {
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

    public List<ApiMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ApiMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }

    public ApiMetricRange getRange() {
        return range;
    }

    public void setRange(ApiMetricRange range) {
        this.range = range;
    }

    public ApiMetricDateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(ApiMetricDateRange dateRange) {
        this.dateRange = dateRange;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }
}
