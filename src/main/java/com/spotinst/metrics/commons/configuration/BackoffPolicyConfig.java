package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackoffPolicyConfig {
    private Long    initialDelayMs;
    private Integer numOfRetries;
}