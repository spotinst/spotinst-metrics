package com.spotinst.metrics.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Tal.Geva on 07/08/2024.
 */
@Setter
@Getter
public class ApiMetricRange {
    private Double minimum;
    private Double maximum;
    private Double gap;

    public ApiMetricRange() {
    }
}
