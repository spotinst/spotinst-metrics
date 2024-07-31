package com.spotinst.service.commons.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;

public class RedisConfig {
    //region Members
    @NotEmpty
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    private Integer timeout;

    private String password;

    @NotNull
    private Integer poolMaxTotal;

    @NotNull
    private Integer poolMaxIdle;

    @NotNull
    private Integer poolMinIdle;

    @NotNull
    private Integer poolMaxWaitMillis;

    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean testOnCreate;
    private Boolean testWhileIdle;
    private Integer numTestsPerEvictionRun;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;
    //endregion

    //region Getters and Setters
    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public Integer getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty
    public Integer getTimeout() {
        return timeout;
    }

    @JsonProperty
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public Integer getPoolMaxTotal() {
        return poolMaxTotal;
    }

    @JsonProperty
    public void setPoolMaxTotal(Integer poolMaxTotal) {
        this.poolMaxTotal = poolMaxTotal;
    }

    @JsonProperty
    public Integer getPoolMaxIdle() {
        return poolMaxIdle;
    }

    @JsonProperty
    public void setPoolMaxIdle(Integer poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }

    @JsonProperty
    public Integer getPoolMinIdle() {
        return poolMinIdle;
    }

    @JsonProperty
    public void setPoolMinIdle(Integer poolMinIdle) {
        this.poolMinIdle = poolMinIdle;
    }

    @JsonProperty
    public Integer getPoolMaxWaitMillis() {
        return poolMaxWaitMillis;
    }

    @JsonProperty
    public void setPoolMaxWaitMillis(Integer poolMaxWaitMillis) {
        this.poolMaxWaitMillis = poolMaxWaitMillis;
    }

    @JsonProperty
    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    @JsonProperty
    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    @JsonProperty
    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    @JsonProperty
    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    @JsonProperty
    public Boolean getTestOnCreate() {
        return testOnCreate;
    }

    @JsonProperty
    public void setTestOnCreate(Boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    @JsonProperty
    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    @JsonProperty
    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    @JsonProperty
    public Integer getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    @JsonProperty
    public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    @JsonProperty
    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    @JsonProperty
    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @JsonProperty
    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    @JsonProperty
    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    //endregion
}