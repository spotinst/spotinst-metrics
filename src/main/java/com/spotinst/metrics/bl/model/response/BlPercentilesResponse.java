package com.spotinst.metrics.bl.model.response;

import com.spotinst.metrics.bl.model.BlPercentileAggregationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlPercentilesResponse {
    //region Members
    private Integer                             totalHits;
    private String                              lastReportDate;
    private List<BlPercentileAggregationResult> percentiles;
    //endregion
}
