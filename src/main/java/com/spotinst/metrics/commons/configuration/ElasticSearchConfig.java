package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
public class ElasticSearchConfig {
    @NotNull
    private String host;

    @NotNull
    @Max(65535)
    @Min(1024)
    private Integer port;

    @NotNull
    private String clusterName;

    private Boolean           transportSniff;
    private Integer           pingTimeout;
    private IndexNamePatterns indexNamePatterns;
    private Set<String>       indexNameOverriddenPatterns;
}
