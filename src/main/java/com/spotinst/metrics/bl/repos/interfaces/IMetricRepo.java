package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.model.BlMetricStatistics;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;

import java.util.List;

public interface IMetricRepo {
    RepoGenericResponse<EsMetricReportResponse> create(BlMetricCreateRequest request);

    RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                       String index);

    EsMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
                                                String index) throws DalException;
}
