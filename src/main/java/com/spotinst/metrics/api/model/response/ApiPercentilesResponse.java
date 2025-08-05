package com.spotinst.metrics.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotinst.commons.response.api.items.IServiceResponseItem;
import com.spotinst.metrics.api.model.ApiPercentileAggregationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiPercentilesResponse implements IServiceResponseItem {
    //region Members
    private Integer                              totalHits;
    private String                               lastReportDate;
    private List<ApiPercentileAggregationResult> percentiles;
    //endregion
}
