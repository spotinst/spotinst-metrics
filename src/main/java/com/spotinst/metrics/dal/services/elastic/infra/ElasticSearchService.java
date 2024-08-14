package com.spotinst.metrics.dal.services.elastic.infra;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;
import com.spotinst.metrics.dal.services.elastic.metric.ElasticMetricService;
import com.spotinst.metrics.dal.services.elastic.metric.IElasticMetricService;

import java.util.List;

public class ElasticSearchService extends BaseElasticSearchService {
    private IElasticMetricService metricService;

    public ElasticSearchService() {
        super();
        this.metricService = new ElasticMetricService();
//        this.metricService = new ElasticMetricService(transportClient, bulkProcessor);
    }

    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest esMetricStatisticsRequest,
                                                                String index)
            throws DalException {
        return metricService.getMetricsStatistics(esMetricStatisticsRequest, index);
    }

    @Override
    public EsMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments, String index)
            throws DalException {
        return metricService.reportMetrics(esMetricDocuments, index);
    }
}
