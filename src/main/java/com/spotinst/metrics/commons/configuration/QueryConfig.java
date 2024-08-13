package com.spotinst.metrics.commons.configuration;

import javax.validation.constraints.NotNull;

public class QueryConfig {
    @NotNull
    private Integer dimensionsKeysRequestLimit;

    @NotNull
    private Integer dimensionsValuesResultPerBucketLimit;

    @NotNull
    private Integer queryTimeLimitInDays;

    public QueryConfig() {
    }

    public Integer getDimensionsKeysRequestLimit() {
        return dimensionsKeysRequestLimit;
    }

    public void setDimensionsKeysRequestLimit(Integer dimensionsKeysRequestLimit) {
        this.dimensionsKeysRequestLimit = dimensionsKeysRequestLimit;
    }

    public Integer getDimensionsValuesResultPerBucketLimit() {
        return dimensionsValuesResultPerBucketLimit;
    }

    public void setDimensionsValuesResultPerBucketLimit(Integer dimensionsValuesResultPerBucketLimit) {
        this.dimensionsValuesResultPerBucketLimit = dimensionsValuesResultPerBucketLimit;
    }

    public Integer getQueryTimeLimitInDays() {
        return queryTimeLimitInDays;
    }

    public void setQueryTimeLimitInDays(Integer queryTimeLimitInDays) {
        this.queryTimeLimitInDays = queryTimeLimitInDays;
    }
}
