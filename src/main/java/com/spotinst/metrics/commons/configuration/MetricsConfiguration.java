package com.spotinst.metrics.commons.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotinst.dropwizard.common.configuration.BaseApplicationConfiguration;
import com.spotinst.dropwizard.common.ratelimit.RateLimitConfig;
import com.spotinst.metrics.commons.configuration.ratelimit.DbOrganizationMappingConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MetricsConfiguration extends BaseApplicationConfiguration {
    //region Getters and Setters
    //region Members
    @Valid
    @NotNull
    private ServicesConfig services;

    // Optional when a rate limit behaviour per organization is required
    private RateLimitConfig                    rateLimit;
    private DbOrganizationMappingConfiguration dbMapping;


    @Valid
    @NotNull
    private RedisConfig redisConfiguration;

    @Valid
    @NotNull
    private ElasticConfig elastic;

    @Valid
    @NotNull
    private QueryConfig queryConfig;

    @Valid
    @NotNull
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @Valid
    @NotNull
    private IndexConfig indexConfig;

    @Valid
    @NotNull
    private MetadataCacheConfig metadataCache;


    //endregion

}
