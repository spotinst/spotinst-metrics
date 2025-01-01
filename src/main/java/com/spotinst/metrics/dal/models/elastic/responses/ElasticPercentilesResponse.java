package com.spotinst.metrics.dal.models.elastic.responses;

import com.spotinst.metrics.dal.models.elastic.ElasticPercentileAggregationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ElasticPercentilesResponse {
    //region Members
    private Integer                                  totalHits;
    private String                                   lastReportDate;
    private List<ElasticPercentileAggregationResult> percentiles;
    //endregion
}
