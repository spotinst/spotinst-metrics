package com.spotinst.metrics.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.metrics.api.model.ApiMetricDateRange;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiMetricRange;
import io.dropwizard.validation.OneOf;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@Setter
@Getter
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
}
