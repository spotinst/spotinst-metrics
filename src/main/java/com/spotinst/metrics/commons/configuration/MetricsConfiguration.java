package com.spotinst.metrics.commons.configuration;

import com.spotinst.dropwizard.common.configuration.BaseApplicationConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MetricsConfiguration extends BaseApplicationConfiguration {
    //region Members
    @Valid
    @NotNull
    private ServicesConfig services;

    @Valid
    @NotNull
    private RedisConfig redisConfiguration;

    @Valid
    @NotNull
    private ElasticConfig elastic;

    @Valid
    @NotNull
    private QueryConfig queryConfig;

    //todo tal oyar - do we need the metadaCache? seems like it was only being used by lb-core.
    //    @Valid
    //    @NotNull
    //    private MetadataCacheConfig metadataCache;
    //endregion

}
