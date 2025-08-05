package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkProcessorIndexerConfig {
    private Integer             bufferedActions;
    private Integer             concurrentRequests;
    private Long                flushIntervalInSeconds;
    private Long                sizeInMB;
    private BackoffPolicyConfig backoffPolicyConfig;
}