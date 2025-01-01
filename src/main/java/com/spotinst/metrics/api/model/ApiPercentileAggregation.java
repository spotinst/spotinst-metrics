package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiPercentileAggregation {
    //region Members
    @NotEmpty
    private String aggName;

    @NotEmpty
    private String metricName;

    @Range(min = 0, max = 100)
    private Double percentile;
    //endregion
}
