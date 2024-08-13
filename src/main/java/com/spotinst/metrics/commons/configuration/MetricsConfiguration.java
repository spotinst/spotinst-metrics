package com.spotinst.metrics.commons.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotinst.dropwizard.common.configuration.BaseApplicationConfiguration;
import com.spotinst.dropwizard.common.ratelimit.RateLimitConfig;
import com.spotinst.metrics.commons.configuration.ratelimit.DbOrganizationMappingConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class MetricsConfiguration extends BaseApplicationConfiguration {
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
    //endregion

    //region Getters and Setters
    public ServicesConfig getServices() {
        return services;
    }

    public void setServices(ServicesConfig services) {
        this.services = services;
    }

    public DbOrganizationMappingConfiguration getDbMapping() {
        return dbMapping;
    }

    public void setDbMapping(DbOrganizationMappingConfiguration dbMapping) {
        this.dbMapping = dbMapping;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public RedisConfig getRedisConfiguration() {
        return redisConfiguration;
    }

    public void setRedisConfiguration(RedisConfig redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    public ElasticConfig getElastic() {
        return elastic;
    }

    public void setElastic(ElasticConfig elastic) {
        this.elastic = elastic;
    }

    public QueryConfig getQueryConfig() {
        return queryConfig;
    }

    public void setQueryConfig(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
        return swaggerBundleConfiguration;
    }

    public void setSwaggerBundleConfiguration(SwaggerBundleConfiguration swaggerBundleConfiguration) {
        this.swaggerBundleConfiguration = swaggerBundleConfiguration;
    }
    //endregion

}
