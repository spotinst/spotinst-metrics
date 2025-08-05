package com.spotinst.metrics.dal.models.elastic.responses;

import com.spotinst.metrics.dal.models.elastic.ElasticMetricResultStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticMetricReportResponse {
    private ElasticMetricResultStatus status;
}
