package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.common.unit.TimeValue;

@Getter
@Setter
public class BackoffPolicyConfig {
    private Long    initialDelayMs;
    private Integer numOfRetries;

    public BackoffPolicy toBackoffPolicy() {
        return BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(initialDelayMs), numOfRetries);
    }
}