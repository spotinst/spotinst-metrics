package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Tal.Geva on 07/08/2024.
 */

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetric {
    private String name;
    private Double value;
    private String unit;

    // If no count value supplied, Jackson won't trigger the setter and count value would be 1
    private Long count = 1L;

    public ApiMetric() {
    }
}
