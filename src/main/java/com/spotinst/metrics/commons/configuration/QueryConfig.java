package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class QueryConfig {
    @NotNull
    private Integer dimensionsKeysRequestLimit;

    @NotNull
    private Integer dimensionsValuesResultPerBucketLimit;

    @NotNull
    private Integer queryTimeLimitInDays;

    public QueryConfig() {
    }

}
