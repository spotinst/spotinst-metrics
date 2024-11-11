package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricReportResponse;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;

public interface IMetricRepo {
    RepoGenericResponse<BlMetricReportResponse> report(BlMetricReportRequest request, String index);

    RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                         String index);
}
