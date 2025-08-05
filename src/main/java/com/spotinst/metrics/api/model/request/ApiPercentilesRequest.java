package com.spotinst.metrics.api.model.request;

import com.spotinst.metrics.api.model.ApiMetricDateRange;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiPercentileAggregation;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class ApiPercentilesRequest {
    //region Members
    @NotEmpty
    private String namespace;

    @Size(min = 1)
    private List<ApiMetricDimension> dimensions;

    @Valid
    @NotNull
    private ApiMetricDateRange dateRange;

    @Size(min = 1)
    @Valid
    private List<ApiPercentileAggregation> percentiles;

    private Boolean filterZeroes;
    //endregion
}
