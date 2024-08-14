package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.index.spotinst.RawIndexManager;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.commons.utils.EsIndexNamingUtils;
import com.spotinst.metrics.dal.dao.elastic.MetricDAO;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.EsMetricResultStatus;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricReportRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;
import com.spotinst.metrics.dal.services.elastic.infra.ElasticSearchService;
import com.spotinst.metrics.dal.services.elastic.infra.IElasticSearchService;
import com.spotinst.metrics.dal.services.elastic.metric.ElasticMetricService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateDailyIndexName;

/**
 * @author Tal.Geva
 * @since 07/08/2024
 */
public class MetricRepo implements IMetricRepo {

    //region Members
    private static final Logger        LOGGER = LoggerFactory.getLogger(MetricRepo.class);
    private BulkProcessor         bulkProcessor;
    private IElasticSearchService elasticService;

    public MetricRepo() {
        initInjections();
    }

    private void initInjections() {
        this.elasticService = MetricsAppContext.getInstance().getElasticSearchService();
    }


    // Document type
    private static final String DEFAULT_DOC_TYPE = "balancer";

    //endregion

    //region Override Methods
    @Override
    public RepoGenericResponse<EsMetricReportResponse> report(BlMetricReportRequest request, String index) {
        RepoGenericResponse<EsMetricReportResponse> retVal;

        ElasticMetricReportRequest  esMetricReportRequest = MetricReportConverter.toEs(request);
        List<ElasticMetricDocument> dalMetrics            = esMetricReportRequest.getMetricDocuments();

        try {
//            EsMetricReportResponse reportResponse = elasticService.reportMetrics(dalMetrics, index);
            EsMetricReportResponse reportResponse = reportMetrics(dalMetrics, index);
            retVal = new RepoGenericResponse<>(reportResponse);
        }
        catch (DalException ex) {
            LOGGER.error("Failed to report metrics in ES. Error: {}", ex.getMessage());

            DalException dalException = new DalException(ex.getMessage(), ex.getCause());
            retVal = ExceptionHelper.handleDalException(dalException);
        }

        return retVal;
    }

    public EsMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments, String index) throws DalException {
        EsMetricReportResponse retVal = new EsMetricReportResponse();

        if (CollectionUtils.isEmpty(esMetricDocuments)) {
            String errMsg = "Cannot report metrics to elastic search, no metrics to report";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }
        else {
            int docCount = esMetricDocuments.size();
            LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));
            BulkRequest request = new BulkRequest();

            for (ElasticMetricDocument doc : esMetricDocuments) {
                String json = JsonMapper.toJson(doc);

                if (StringUtils.isEmpty(json)) {
                    String errMsg = "Cannot report metric to elastic search, failed serializing to JSON. Metric: %s";
                    LOGGER.error(errMsg, doc.toString());
                }
                else {
                    index = EsIndexNamingUtils.generateDailyIndexName(index);
                    request.add(new IndexRequest(index).source(json,
                                                               XContentType.JSON)); // add documents to the bulk request
                }
            }

            try {
                MetricsAppContext.getInstance().getElasticClient().bulk(request, RequestOptions.DEFAULT);
            }
            catch (IOException e) {
                LOGGER.error(String.format("Failed to report metrics to elastic search. Request: %s", request));
            }

            EsMetricResultStatus status = new EsMetricResultStatus();
            status.setCode(RestStatus.OK.getStatus());
            status.setName(RestStatus.OK.name());
            retVal.setStatus(status);
        }
        return retVal;
    }


    @Override
    public RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                                String index) {
        RepoGenericResponse<BlMetricStatisticsResponse> retVal;

        try {
            ElasticMetricStatisticsRequest  esGetMetricStatsRequest = MetricStatisticConverter.toEs(request);
//                        ElasticMetricStatisticsResponse
//                                            esMetricResp            = elasticService.getMetricsStatistics(esGetMetricStatsRequest, index);
            ElasticMetricStatisticsResponse
                    esMetricResp            = getMetricsStatistics(esGetMetricStatsRequest, index);
            //            List<ElasticMetricStatistics> dalMetrics = dao.getAll(request, index);
//            MetricDAO dao       = new MetricDAO();

//            ElasticMetricStatisticsResponse
//                    esMetricResp = dao.getMetricsStatistics(esGetMetricStatsRequest, index);

            // Exclude metric documents with empty data points
//            List<BlMetricStatistics> blMetricResp = MetricStatisticConverter.dalToBl(dalMetrics);
            BlMetricStatisticsResponse blMetricResp = MetricStatisticConverter.toBl(esMetricResp);
            retVal = new RepoGenericResponse<>(blMetricResp);
        }
        catch (DalException e) {
            retVal = ExceptionHelper.handleDalException(e);
        }

        return retVal;
    }

    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest request,
                                                                String index) throws DalException {

        ElasticMetricStatisticsResponse retVal;

        try {
            ElasticMetricDateRange dateRange = request.getDateRange();
            //            String[]               indices      = generateIndicesByDateRange(dateRange, index);
            //            String                 indicesNames = logIndicesNames(indices);
            String[] indices = new String[1];
            indices[0] = index;

            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
            // New way using RestHighLevelClient
            SearchRequest searchRequest = new SearchRequest(indices);
            searchRequest.indicesOptions(options);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            searchRequest.source(sourceBuilder.query(QueryBuilders.matchAllQuery()).explain(false));

            RawIndexManager manager = new RawIndexManager(request);

            // Set request query
            manager.setFilters(sourceBuilder);
            manager.setAggregations(sourceBuilder);
            SearchResponse searchResponse =
                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
            //
            // Execute
            StopWatch sw = new StopWatch();
            sw.start();
            sw.stop();
//            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms", indicesNames,
//                                                  DEFAULT_DOC_TYPE, sw.totalTime().millis()));

            // Parse elastic search response
            retVal = manager.parseResponse(searchResponse);

        }
        catch (Exception ex) {
            String errFormat = "Failed getting metric statistics from elastic search for request: %s";
            String errMsg    = String.format(errFormat, request.toString());
            LOGGER.error(errMsg, ex);
            throw new DalException(errMsg, ex);
        }

        LOGGER.debug("Finished fetching metrics statistics by request filter");

        return retVal;
    }
}
