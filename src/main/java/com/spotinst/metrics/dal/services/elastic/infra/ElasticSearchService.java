package com.spotinst.metrics.dal.services.elastic.infra;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.metric.ElasticMetricService;
import com.spotinst.metrics.dal.services.elastic.metric.IElasticMetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ElasticSearchService extends BaseElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);

    private IElasticMetricService metricService;

    public ElasticSearchService() {
        super();
        this.metricService = new ElasticMetricService(elasticsearchClient, bulkIngester);
    }

    //todo tal - this only being used in LB resource

    //        @Override
    //        public ElasticMetricStatisticsResponse getAggregatedMetricsStatistics(
    //                ElasticMetricStatisticsRequest esMetricStatisticsRequest)
    //                throws DalException {
    //            return metricService.g(esMetricStatisticsRequest);
    //        }

    // region Metric
    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(
            ElasticMetricStatisticsRequest esMetricStatisticsRequest, String index) throws DalException {
        return metricService.getMetricsStatistics(esMetricStatisticsRequest, index);
    }

    @Override
    public ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest esDimensionsValuesRequest,
                                                               String index) throws DalException {
        return metricService.getDimensionsValues(esDimensionsValuesRequest, index);
    }

    @Override
    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
                                                     String index) throws DalException {
        return metricService.reportMetrics(esMetricDocuments, index);
    }
    //endregion
}