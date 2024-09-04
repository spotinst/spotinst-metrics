package com.spotinst.metrics.dal.models.elastic.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ElasticDimensionsValuesRequest {
    private String       accountId;
    private String       namespace;
    private List<String> dimensionKeys;

    @Override
    public String toString() {
        return "EsMetricStatisticsRequest{" + "accountId='" + accountId + '\'' + ", namespace='" + namespace + '\'' +
               ", dimensionKeys=" + dimensionKeys + '}';
    }
}
