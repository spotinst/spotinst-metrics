//package com.spotinst.metrics.dal.services.elastic.metric;
//
//import com.spotinst.commons.mapper.json.JsonMapper;
//import com.spotinst.dropwizard.common.exceptions.dal.DalException;
//import com.spotinst.metrics.MetricsAppContext;
//import com.spotinst.metrics.bl.index.spotinst.RawIndexManager;
//import com.spotinst.metrics.commons.utils.EsIndexNamingUtils;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
//import com.spotinst.metrics.dal.models.elastic.ElasticMetricResultStatus;
//import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
//import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
//import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.support.IndicesOptions;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.common.StopWatch;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.rest.RestStatus;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.List;
//
//import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateIndicesByDateRange;
//import static com.spotinst.metrics.dal.services.elastic.infra.ElasticSearchService.logIndicesNames;
//
//public class ElasticMetricService implements IElasticMetricService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticMetricService.class);
//
//    private static final String DEFAULT_DOC_TYPE = "balancer";
//
//    public ElasticMetricService() {
//    }
//
//    @Override
//    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
//                                                     String index) throws DalException {
//        ElasticMetricReportResponse retVal   = new ElasticMetricReportResponse();
//        int                         docCount = esMetricDocuments.size();
//        LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));
//        BulkRequest request = new BulkRequest();
//        index = EsIndexNamingUtils.generateDailyIndexName(index); //TODO Tal: In lb-metrics it's different implementation
//
//        for (ElasticMetricDocument doc : esMetricDocuments) {
//            String json = JsonMapper.toJson(doc);
//
//            if (StringUtils.isEmpty(json)) {
//                String errMsg = "Cannot report metric to elastic search, failed serializing to JSON. Metric: %s";
//                LOGGER.error(errMsg, doc.toString());
//            }
//            else {
//                request.add(
//                        new IndexRequest(index).source(json, XContentType.JSON)); // add documents to the bulk request
//            }
//        }
//        try {
//            MetricsAppContext.getInstance().getElasticClient().bulk(request, RequestOptions.DEFAULT); //TODO Tal: This is different
//        }
//        catch (IOException e) {
//            LOGGER.error(String.format("Failed to report metrics to elastic search. Request: %s", request));
//        }
//
//        ElasticMetricResultStatus status = new ElasticMetricResultStatus();
//        status.setCode(RestStatus.OK.getStatus());
//        status.setName(RestStatus.OK.name());
//        retVal.setStatus(status);
//        return retVal;
//    }
//
//    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest request,
//                                                                String index) throws DalException {
//
//        ElasticMetricStatisticsResponse retVal;
//
//        try {
//            ElasticMetricDateRange dateRange = request.getDateRange();
//            String[]               indices      = generateIndicesByDateRange(dateRange, index);
//            String                 indicesNames = logIndicesNames(indices);
//
//            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
//            // New way using RestHighLevelClient
//            SearchRequest searchRequest = new SearchRequest(indices);
//            searchRequest.indicesOptions(options);
//            searchRequest.requestCache(true);
//            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//            searchRequest.source(sourceBuilder.query(QueryBuilders.matchAllQuery()).explain(false));
//            //            sourceBuilder.explain(false); // don't show scoring
//            RawIndexManager manager = new RawIndexManager(request);
//
//            // Set request query
//            manager.setFilters(sourceBuilder);
//            manager.setAggregations(sourceBuilder);
//            //
//            // Execute
//            StopWatch sw = new StopWatch();
//            sw.start();
//            SearchResponse searchResponse =
//                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
//            sw.stop();
//            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms", indicesNames,
//                                      DEFAULT_DOC_TYPE, sw.totalTime().millis()));
//
//            // Parse elastic search response
//            retVal = manager.parseResponse(searchResponse);
//
//        }
//        catch (Exception ex) {
//            String errFormat = "Failed getting metric statistics from elastic search for request: %s";
//            String errMsg    = String.format(errFormat, request.toString());
//            LOGGER.error(errMsg, ex);
//            throw new DalException(errMsg, ex);
//        }
//
//        LOGGER.debug("Finished fetching metrics statistics by request filter");
//
//        return retVal;
//    }
//}


package com.spotinst.metrics.dal.services.elastic.metric;

//import com.spotinst.metrics.bl.index.custom.DimensionsIndexManager;
//import com.spotinst.metrics.bl.index.spotinst.AggregationIndexManager;
import com.spotinst.metrics.bl.index.spotinst.RawIndexManager;
import com.spotinst.metrics.commons.utils.EsIndexNamingUtils;
//import com.spotinst.metrics.commons.utils.EsMetricTimeUtils;
//import com.spotinst.metrics.commons.utils.EsTimeUnit;
//import com.spotinst.metrics.dal.model.es.metric.EsMetricDateRange;
//import com.spotinst.metrics.dal.model.es.metric.EsMetricDocument;
//import com.spotinst.metrics.dal.model.es.metric.EsMetricResultStatus;
//import com.spotinst.metrics.dal.model.es.metric.request.EsDimensionsValuesRequest;
//import com.spotinst.metrics.dal.model.es.metric.request.EsMetricStatisticsRequest;
//import com.spotinst.metrics.dal.model.es.metric.response.EsDimensionsValuesResponse;
//import com.spotinst.metrics.dal.model.es.metric.response.EsMetricReportResponse;
//import com.spotinst.metrics.dal.model.es.metric.response.EsMetricStatisticsResponse;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricResultStatus;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateDailyIndexName;
import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateIndicesByDateRange;

/**
 * Created by zachi.nachshon on 1/19/17.
 */
public class ElasticMetricService implements IElasticMetricService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticMetricService.class);

    private RestHighLevelClient restHighLevelClient;
    private BulkProcessor bulkProcessor;

    // Document type
    private static final String DEFAULT_DOC_TYPE = "_doc";

    public ElasticMetricService(RestHighLevelClient restHighLevelClient, BulkProcessor bulkProcessor) {
        this.restHighLevelClient = restHighLevelClient;
        this.bulkProcessor = bulkProcessor;
    }

    private void indexBulk(List<String> documents, String index) throws DalException {
        try {
            String idxName = generateDailyIndexName(index);

            for (String document : documents) {
                byte[] byteReq = document.getBytes(Charset.forName("UTF-8"));
                IndexRequest request = new IndexRequest(idxName).source(byteReq);

                String queryMessage =
                        "About to async bulk index the following document on elasticSearch on index [%s]:\n %s\ntypes: [%s]";
                LOGGER.debug(String.format(queryMessage, idxName, document, DEFAULT_DOC_TYPE));

                bulkProcessor.add(request);
            }
        } catch (Exception ex) {
            String errMsg = "Failed to index document to elastic search !";
            LOGGER.error(errMsg, ex);
            throw new DalException(errMsg, ex);
        }
    }

    @Override
    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments, String index) throws DalException {
        ElasticMetricReportResponse retVal = new ElasticMetricReportResponse();

        if (CollectionUtils.isEmpty(esMetricDocuments)) {
            String errMsg = "Cannot report metric(s) to elastic search, no metrics to report";
            LOGGER.warn(errMsg);
            throw new DalException(errMsg);
        } else {
            int docCount = esMetricDocuments.size();
            LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));

            List<String> documents = new ArrayList<>();
            for (ElasticMetricDocument doc : esMetricDocuments) {
                String json = JsonMapper.toJson(doc);

                if (StringUtils.isEmpty(json)) {
                    String errMsg = "Cannot report metric to elastic search, failed serializing to JSON. Metric: %s";
                    LOGGER.error(errMsg, doc.toString());
                    // Do not throw exception, let the bulk indexing process complete its action
                } else {
                    documents.add(json);
                }
            }

            indexBulk(documents, index);

            LOGGER.debug(String.format("After bulk indexing [%s] metrics to elastic search", documents.size()));

            // Since we're using bulk indexing and ES flushing data asynchronously -
            // return success if we passed indexBulk step
            ElasticMetricResultStatus status = new ElasticMetricResultStatus();
            status.setCode(RestStatus.OK.getStatus());
            status.setName(RestStatus.OK.name());
            retVal.setStatus(status);
        }

        return retVal;
    }

//    @Override
//    public EsMetricStatisticsResponse getAggregatedMetricsStatistics(EsMetricStatisticsRequest request) throws DalException {
//        EsMetricStatisticsResponse retVal;
//        Boolean useAggregationIndex = true;
//
//        try {
//            EsMetricDateRange dateRange = request.getDateRange();
//            String[] indices = EsIndexNamingUtils.generateIndicesByDateRange(dateRange, null, useAggregationIndex);
//            String indicesNames = logIndicesNames(indices);
//
//            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
//            SearchRequest searchRequest = new SearchRequest(indices);
//            searchRequest.indicesOptions(options);
//
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//            searchRequest.source(searchSourceBuilder);
//
//            AggregationIndexManager manager = new AggregationIndexManager(request);
//
//            // Set request query
//            manager.setFilters(searchSourceBuilder);
//            manager.setAggregations(searchSourceBuilder);
//
//            // Execute
//            StopWatch sw = new StopWatch();
//            sw.start();
//            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            sw.stop();
//            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms",
//                                      indicesNames, DEFAULT_DOC_TYPE, sw.totalTime().millis()));
//
//            retVal = manager.parseResponse(searchResponse);
//
//        } catch (Exception ex) {
//            String errFormat = "Failed getting metric statistics from elastic search for request: %s";
//            String errMsg = String.format(errFormat, request.toString());
//            LOGGER.error(errMsg, ex);
//            throw new DalException(errMsg, ex);
//        }
//
//        LOGGER.debug("Finished fetching metrics statistics by request filter");
//
//        return retVal;
//    }

    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest request, String index) throws DalException {
        ElasticMetricStatisticsResponse retVal;

        try {
            ElasticMetricDateRange dateRange = request.getDateRange();
            String[]               indices   = generateIndicesByDateRange(dateRange, index);
            String indicesNames = logIndicesNames(indices);

            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
            SearchRequest searchRequest = new SearchRequest(indices);
            searchRequest.indicesOptions(options);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            RawIndexManager manager = new RawIndexManager(request);

            // Set request query
            manager.setFilters(searchSourceBuilder);
            manager.setAggregations(searchSourceBuilder);

            // Execute
            StopWatch sw = new StopWatch();
            sw.start();
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            sw.stop();
            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms",
                                      indicesNames, DEFAULT_DOC_TYPE, sw.totalTime().millis()));

            // Parse elastic search response
            retVal = manager.parseResponse(searchResponse);

        } catch (Exception ex) {
            String errFormat = "Failed getting metric statistics from elastic search for request: %s";
            String errMsg = String.format(errFormat, request.toString());
            LOGGER.error(errMsg, ex);
            throw new DalException(errMsg, ex);
        }

        LOGGER.debug("Finished fetching metrics statistics by request filter");

        return retVal;
    }

//    @Override
//    public EsDimensionsValuesResponse getDimensionsValues(EsDimensionsValuesRequest request, String index) throws DalException {
//        EsDimensionsValuesResponse retVal;
//
//        try {
//            EsTimeUnit timeUnit = EsMetricTimeUtils.createOffsetTimeUnitFromNow(TimeUnit.DAYS, 1);
//
//            EsMetricDateRange dateRange = new EsMetricDateRange();
//            dateRange.setFrom(timeUnit.getFromDate());
//            dateRange.setTo(timeUnit.getToDate());
//
//            String[] indices = generateIndicesByDateRange(dateRange, index);
//            String indicesNames = logIndicesNames(indices);
//
//            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
//            SearchRequest searchRequest = new SearchRequest(indices);
//            searchRequest.indicesOptions(options);
//
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//            searchRequest.source(searchSourceBuilder);
//
//            List<String> dimensionKeys = request.getDimensionKeys();
//            if (dimensionKeys != null && !dimensionKeys.isEmpty()) {
//                // Add dimension keys to the query
//            }
//
//            DimensionsIndexManager manager = new DimensionsIndexManager(request);
//
//            // Set request query
//            manager.setFilters(searchSourceBuilder);
//            manager.setAggregations(searchSourceBuilder);
//
//            // Execute
//            StopWatch sw = new StopWatch();
//            sw.start();
//            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            sw.stop();
//            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms",
//                                      indicesNames, DEFAULT_DOC_TYPE, sw.totalTime().millis()));
//
//            // Parse elastic search response
//            retVal = manager.parseResponse(searchResponse);
//
//        } catch (Exception ex) {
//            String errFormat = "Failed getting dimensions values from elastic search for request: %s";
//            String errMsg = String.format(errFormat, request.toString());
//            LOGGER.error(errMsg, ex);
//            throw new DalException(errMsg, ex);
//        }
//
//        LOGGER.info("Finished fetching dimensions values");
//
//        return retVal;
//    }

    private String logIndicesNames(String[] indices) {
        StringBuilder sb = new StringBuilder();
        if (indices != null && indices.length > 0) {
            for (String idx : indices) {
                sb.append(idx).append(", ");
            }
        }

        String retVal = sb.toString();
        LOGGER.debug(String.format("Starting to fetch metrics statistics from indices [%s]...", retVal));

        return retVal;
    }
}