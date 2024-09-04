package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
//import com.spotinst.metrics.commons.converters.DimensionsValuesConverter;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricReportRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.infra.IElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    public RepoGenericResponse<ElasticMetricReportResponse> report(BlMetricReportRequest request, String index) {
        RepoGenericResponse<ElasticMetricReportResponse> retVal;
        List<ElasticMetricDocument>                      dalMetrics = new ArrayList<>();

        try { //TODO Tal: I can remove this try and catch
            ElasticMetricReportRequest esMetricReportRequest = MetricReportConverter.toEs(request);
            dalMetrics = esMetricReportRequest.getMetricDocuments();
        }
        catch (BlException exception) {
            String errMsg = String.format("Cannot report metrics to elastic search, no metrics to report. Request: %s",
                                          request.toString());
            LOGGER.error(errMsg);
        }

        try {
            ElasticMetricReportResponse esMetricResponse = elasticService.reportMetrics(dalMetrics, index);
//            BlMetricReportResponse blMetricResponse = MetricReportConverter.esToBl(esMetricResponse);

            retVal = new RepoGenericResponse<>(esMetricResponse);
        }
        catch (DalException ex) {
            LOGGER.error("Failed to report metrics in ES. Error: {}", ex.getMessage());
            DalException dalException = new DalException(ex.getMessage(), ex.getCause());
            retVal = ExceptionHelper.handleDalException(dalException);
        }

        return retVal;
    }

    @Override
    public RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                                String index) {
        RepoGenericResponse<BlMetricStatisticsResponse> retVal;

        try {
            ElasticMetricStatisticsRequest  esGetMetricStatsRequest = MetricStatisticConverter.toEs(request);
            ElasticMetricStatisticsResponse esMetricResp            =
                    elasticService.getMetricsStatistics(esGetMetricStatsRequest, index);
            BlMetricStatisticsResponse      blMetricResp            = MetricStatisticConverter.toBl(esMetricResp);
            retVal = new RepoGenericResponse<>(blMetricResp);
        }
        catch (DalException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

//    @Override
//    public RepoGenericResponse<BlDimensionsValuesResponse> getDimensionsValues(BlDimensionsValuesRequest request,
//                                                                               String index) {
//        RepoGenericResponse<BlDimensionsValuesResponse> retVal;
//
//        try {
//            ElasticDimensionsValuesRequest elasticGetMetricStatsRequest = DimensionsValuesConverter.toEs(request);
//            ElasticDimensionsValuesResponse elasticMetricResp =
//                    elasticService.getDimensionsValues(elasticGetMetricStatsRequest, index);
//
//            BlDimensionsValuesResponse blMetricResp = DimensionsValuesConverter.toBl(elasticMetricResp);
//            retVal = new RepoGenericResponse<>(blMetricResp);
//        }
//        catch (DalException ex) {
//            retVal = ExceptionHelper.handleDalException(ex);
//        }
//
//        return retVal;
//    }
}
