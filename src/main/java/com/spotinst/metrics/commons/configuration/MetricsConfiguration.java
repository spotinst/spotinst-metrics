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
    private RedisConfig redisConfiguration;

    @Valid
    @NotNull
    private ElasticConfig elastic;

    @Valid
    @NotNull
    private QueryConfig queryConfig;

    //endregion

}
