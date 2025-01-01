package com.spotinst.metrics.dal.models.elastic.requests;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticPercentileAggregation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ElasticPercentilesRequest {
    //region Members
    private String                        accountId;
    private String                       namespace;
    private List<ElasticMetricDimension>  dimensions;
    private ElasticMetricDateRange             dateRange;
    private List<ElasticPercentileAggregation> percentiles;
    private Boolean                            filterZeroes;
    //endregion
}
