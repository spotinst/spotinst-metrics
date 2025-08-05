package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class MetadataCacheConfig {

    @NotNull
    private Integer maxCachedDimensionsLimitPerAccount;

    @NotNull
    private Integer maxCachedMetricsLimitPerAccountDimension;

    @NotNull
    private Integer updateCacheThreadPoolSize;

    public MetadataCacheConfig() {}

}