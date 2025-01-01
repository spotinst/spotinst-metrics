package com.spotinst.metrics.dal.services.elastic.metric;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;

import java.util.List;

public interface IElasticMetricService {
    ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments, String index) throws DalException;

    ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest esMetricStatisticsRequest,
                                                         String index) throws DalException;

    ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest request, String index) throws DalException;
}
