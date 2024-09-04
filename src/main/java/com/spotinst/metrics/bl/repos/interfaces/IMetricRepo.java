package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;

public interface IMetricRepo {
    RepoGenericResponse<ElasticMetricReportResponse> report(BlMetricReportRequest request, String index);

    RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                       String index);
}
