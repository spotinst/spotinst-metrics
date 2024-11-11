package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndexConfig {
    private Integer reportMetricThreadPoolSize;

    public IndexConfig() {}

}
