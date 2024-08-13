package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.model.BlMetricStatistics;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.dal.dao.elastic.MetricDAO;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.EsMetricResultStatus;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricReportRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.responses.EsMetricReportResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateDailyIndexName;

/**
 * @author Tal.Geva
 * @since 07/08/2024
 */
public class MetricRepo implements IMetricRepo {

    //region Members
    private static final Logger        LOGGER = LoggerFactory.getLogger(MetricRepo.class);
    private          BulkProcessor bulkProcessor;

    // Document type
    private static final String DEFAULT_DOC_TYPE = "balancer";

    //endregion

    //region Override Methods
    @Override
    public RepoGenericResponse<EsMetricReportResponse> create(BlMetricCreateRequest request) {
        RepoGenericResponse<EsMetricReportResponse> retVal;

        ElasticMetricReportRequest  esMetricReportRequest = MetricReportConverter.toEs(request);
        List<ElasticMetricDocument> dalMetrics            = esMetricReportRequest.getMetricDocuments();

        //        List<ElasticMetricDocument> dalMetrics =
        //                request.getMetricDocuments().stream().map(MetricReportConverter::blToDal).collect(Collectors.toList());
        try {
//            MetricDAO dao       = new MetricDAO();
//            Boolean   isCreated = dao.create(dalMetrics);
            EsMetricReportResponse reportResponse = reportMetrics(dalMetrics, "metrics_report_test");
            retVal = new RepoGenericResponse<>(reportResponse);
        }
        catch (DalException ex) {
            LOGGER.error("Failed to create metrics in ES. Error: {}", ex.getMessage());

            DalException dalException = new DalException(ex.getMessage(), ex.getCause());
            retVal = ExceptionHelper.handleDalException(dalException);
        }

        return retVal;
    }

    @Override
    public EsMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
                                                String index) throws DalException {
        EsMetricReportResponse retVal = new EsMetricReportResponse();

        if (CollectionUtils.isEmpty(esMetricDocuments)) {
            String errMsg = "Cannot report metric(s) to elastic search, no metrics to report";
            LOGGER.warn(errMsg);
            throw new DalException(errMsg);
        }
        else {
            int docCount = esMetricDocuments.size();
            LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));

            BulkRequest request = new BulkRequest(); // create a bulk request

            List<String> documents = new ArrayList<>();
            for (ElasticMetricDocument doc : esMetricDocuments) {
                String json = JsonMapper.toJson(doc);

                if (StringUtils.isEmpty(json)) {
                    String errMsg = "Cannot report metric to elastic search, failed serializing to JSON. Metric: %s";
                    LOGGER.error(errMsg, doc.toString());
                    // Do not throw exception, let the bulk indexing process complete its action
                }
                else {
//                    documents.add(json);
                    request.add(new IndexRequest(index).source(json, XContentType.JSON)); // add documents to the bulk request
                }
            }

//            indexBulk(documents, index);

//            LOGGER.debug(String.format("After bulk indexing [%s] metrics to elastic search", documents.size()));
            try {
                MetricsAppContext.getInstance().getElasticClient().bulk(request, RequestOptions.DEFAULT);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            // Since we're using bulk indexing and ES flushing data asynchronously -
            // return success if we passed indexBulk step
            EsMetricResultStatus status = new EsMetricResultStatus();
            status.setCode(RestStatus.OK.getStatus());
            status.setName(RestStatus.OK.name());
            retVal.setStatus(status);
        }
        return retVal;
    }

//    private void indexBulk(List<String> documents, String index) throws DalException {
//        try {
//            String idxName = generateDailyIndexName(index);
//
//            for(String document : documents) {
//                byte[]       byteReq = document.getBytes(Charset.forName("UTF-8"));
//                IndexRequest request = new IndexRequest(idxName, DEFAULT_DOC_TYPE);
//                request.source(byteReq);
//
//                String queryMessage =
//                        "About to async bulk index the following document on elasticSearch on index [%s]:\n %s\ntypes: [%s]";
//                LOGGER.debug(String.format(queryMessage, idxName, document, DEFAULT_DOC_TYPE));
//
//                bulkProcessor.add(request);
//            }
//        } catch(Exception ex) {
//            String errMsg = "Failed to index document to elastic search !";
//            LOGGER.error(errMsg, ex);
//            throw new DalException(errMsg, ex);
//        }
//    }

    @Override
    public RepoGenericResponse<BlMetricStatisticsResponse> getMetricsStatistics(BlMetricStatisticsRequest request,
                                                                                String index) {
        RepoGenericResponse<BlMetricStatisticsResponse> retVal;
        ElasticMetricStatisticsRequest  esGetMetricStatsRequest = MetricStatisticConverter.toEs(request);

        try {
//            ElasticMetricStatisticsResponse
//                                            esMetricResp            = elasticService.getMetricsStatistics(esGetMetricStatsRequest, index);
            MetricDAO dao       = new MetricDAO();
//            List<ElasticMetricStatistics> dalMetrics = dao.getAll(request, index);

            ElasticMetricStatisticsResponse
                    esMetricResp = dao.getMetricsStatistics(esGetMetricStatsRequest, index);

            // Exclude metric documents with empty data points
//            List<BlMetricStatistics> blMetricResp = MetricStatisticConverter.dalToBl(dalMetrics);
            BlMetricStatisticsResponse blMetricResp = MetricStatisticConverter.toBl(esMetricResp);
            retVal = new RepoGenericResponse<>(blMetricResp);
        }
        catch (DalException e) {
            throw new RuntimeException(e);
        }

        return retVal;
    }
}
