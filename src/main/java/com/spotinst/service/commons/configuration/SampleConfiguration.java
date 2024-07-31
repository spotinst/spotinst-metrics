package com.spotinst.service.commons.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotinst.dropwizard.common.configuration.BaseApplicationConfiguration;
import com.spotinst.dropwizard.common.ratelimit.RateLimitConfig;
import com.spotinst.messaging.configuration.sqs.SqsMessagingConfiguration;
import com.spotinst.service.commons.configuration.ratelimit.DbOrganizationMappingConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class SampleConfiguration extends BaseApplicationConfiguration {
    //region Members
    @Valid
    @NotNull
    private ServicesConfig services;

    // Optional when a messaging producer/consumer behaviour is required
    private SqsMessagingConfiguration messaging;

    // Optional when a rate limit behaviour per organization is required
    private RateLimitConfig                    rateLimit;
    private DbOrganizationMappingConfiguration dbMapping;

    @Valid
    @NotNull
    private RedisConfig redisConfiguration;

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

    public SqsMessagingConfiguration getMessaging() {
        return messaging;
    }

    public void setMessaging(SqsMessagingConfiguration messaging) {
        this.messaging = messaging;
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

    public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
        return swaggerBundleConfiguration;
    }

    public void setSwaggerBundleConfiguration(SwaggerBundleConfiguration swaggerBundleConfiguration) {
        this.swaggerBundleConfiguration = swaggerBundleConfiguration;
    }
    //endregion

}
