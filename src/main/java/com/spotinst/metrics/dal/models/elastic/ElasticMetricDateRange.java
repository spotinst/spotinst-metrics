package com.spotinst.metrics.dal.models.elastic;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ElasticMetricDateRange {
    private Date from;
    private Date to;
}
