package com.spotinst.metrics.commons.configuration;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

@Getter
@Setter
public class BulkProcessorIndexerConfig {
    private Integer             bufferedActions;
    private Integer             concurrentRequests;
    private Long                flushIntervalInSeconds;
    private Long                sizeInMB;
    private BackoffPolicyConfig backoffPolicyConfig;

    public ByteSizeValue getBulkSize() {
        return ByteSizeValue.ofMb(sizeInMB);
    }

    public TimeValue getFlushInterval() {
        return TimeValue.timeValueSeconds(flushIntervalInSeconds);
    }
}