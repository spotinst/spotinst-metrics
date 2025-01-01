package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricDocument {
    @NotNull
    private String namespace;

    @NotNull
    private String timestamp;

    @Size(min = 1)
    private List<ApiMetricDimension> dimensions;

    @Size(min = 1)
    private List<ApiMetric> metrics;

    public ApiMetricDocument() {
    }
}
