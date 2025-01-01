package com.spotinst.metrics.bl.model.request;

import com.spotinst.metrics.bl.model.BlMetricDateRange;
import com.spotinst.metrics.bl.model.BlMetricDimension;
import com.spotinst.metrics.bl.model.BlPercentileAggregation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlPercentilesRequest {
    private String                        accountId;
    private String                        namespace;
    private List<BlMetricDimension>       dimensions;
    private BlMetricDateRange             dateRange;
    private List<BlPercentileAggregation> percentiles;
    private Boolean                       filterZeroes;
}
