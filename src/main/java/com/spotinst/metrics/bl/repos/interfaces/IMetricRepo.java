package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.metrics.bl.model.request.BlDimensionsValuesRequest;
import com.spotinst.metrics.bl.model.request.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.request.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.response.BlDimensionsValuesResponse;
import com.spotinst.metrics.bl.model.response.BlMetricReportResponse;
import com.spotinst.metrics.bl.model.response.BlMetricStatisticsResponse;

public interface IMetricRepo {
    RepoGenericResponse<BlMetricReportResponse> report(BlMetricReportRequest request, String index);

    RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                         String index);

    RepoGenericResponse<BlDimensionsValuesResponse> getDimensionsValues(BlDimensionsValuesRequest request,
                                                                        String index);
}
