package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.model.request.BlDimensionsValuesRequest;
import com.spotinst.metrics.bl.model.request.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.request.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.response.BlDimensionsValuesResponse;
import com.spotinst.metrics.bl.model.response.BlMetricReportResponse;
import com.spotinst.metrics.bl.model.response.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.commons.converters.DimensionsValuesConverter;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricReportRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.infra.IElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Tal.Geva
 * @since 07/08/2024
 */
public class MetricRepo implements IMetricRepo {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRepo.class);

    private IElasticSearchService elasticService;

    public MetricRepo() {
        initInjections();
    }

    private void initInjections() {
        this.elasticService = MetricsAppContext.getInstance().getElasticSearchService();
    }

    //region Override Methods
    @Override
    public RepoGenericResponse<BlMetricReportResponse> report(BlMetricReportRequest request, String index) {
        RepoGenericResponse<BlMetricReportResponse> retVal;

        try {
            ElasticMetricReportRequest  esMetricReportRequest = MetricReportConverter.toEs(request);
            List<ElasticMetricDocument> dalMetrics            = esMetricReportRequest.getMetricDocuments();

            ElasticMetricReportResponse esMetricResponse = elasticService.reportMetrics(dalMetrics, index);
            BlMetricReportResponse      blMetricResponse = MetricReportConverter.toBl(esMetricResponse);
            retVal = new RepoGenericResponse<>(blMetricResponse);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }
    //            LOGGER.error("Failed to report metrics in ES. Error: {}", ex.getMessage());
    //            DalException dalException = new DalException(ex.getMessage(), ex.getCause());
    //        catch (BlException exception) {
    //            String errMsg = String.format("Cannot report metrics to elastic search, no metrics to report. Request: %s",
    //                                          request.toString());
    //            LOGGER.error(errMsg);
    //        }

    //        try {
    //            ElasticMetricReportResponse esMetricResponse = elasticService.reportMetrics(dalMetrics, index);
    ////            BlMetricReportResponse blMetricResponse = MetricReportConverter.esToBl(esMetricResponse);
    //
    //            retVal = new RepoGenericResponse<>(esMetricResponse);
    //        }

    @Override
    public RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                                String index) {
        RepoGenericResponse<BlMetricStatisticsResponse> retVal;

        try {
            ElasticMetricStatisticsRequest esGetMetricStatsRequest = MetricStatisticConverter.toEs(request);
            ElasticMetricStatisticsResponse esMetricResp =
                    elasticService.getMetricsStatistics(esGetMetricStatsRequest, index);

            BlMetricStatisticsResponse blMetricResp = MetricStatisticConverter.toBl(esMetricResp);
            retVal = new RepoGenericResponse<>(blMetricResp);
        }
        catch (DalException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<BlDimensionsValuesResponse> getDimensionsValues(BlDimensionsValuesRequest request,
                                                                               String index) {
        RepoGenericResponse<BlDimensionsValuesResponse> retVal;

        try {
            ElasticDimensionsValuesRequest esGetMetricStatsRequest = DimensionsValuesConverter.toEs(request);
            ElasticDimensionsValuesResponse esDimensionsValuesResponse =
                    elasticService.getDimensionsValues(esGetMetricStatsRequest, index);

            BlDimensionsValuesResponse blDimensionsValuesResponse =
                    DimensionsValuesConverter.toBl(esDimensionsValuesResponse);
            retVal = new RepoGenericResponse<>(blDimensionsValuesResponse);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }
}
