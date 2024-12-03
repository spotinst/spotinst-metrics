//package com.spotinst.metrics.dal.services.elastic.infra;
//
//import com.spotinst.commons.mapper.json.JsonMapper;
//import com.spotinst.dropwizard.common.exceptions.dal.DalException;
//import com.spotinst.metrics.MetricsAppContext;
////import com.spotinst.metrics.bl.index.custom.DimensionsIndexManager;
////import com.spotinst.metrics.bl.index.custom.DimensionsIndexManager;
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
//
//public class ElasticSearchService implements IElasticSearchService{
//    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);
//
//    private static final String DEFAULT_DOC_TYPE = "balancer";
//
//    public ElasticSearchService() {
//    }
//
//    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
//                                                     String index) throws DalException {
//        ElasticMetricReportResponse retVal   = new ElasticMetricReportResponse();
//        int                         docCount = esMetricDocuments.size();
//        LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));
//        BulkRequest request = new BulkRequest();
//        index = EsIndexNamingUtils.generateDailyIndexName(index); //TODO Tal: In lb-analytics it's different implementation
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
////            sourceBuilder.explain(false); // don't show scoring
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
//                                                  DEFAULT_DOC_TYPE, sw.totalTime().millis()));
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
//
////    public ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest request, String index)
////            throws DalException {
////
////        ElasticDimensionsValuesResponse retVal;
////
////        try {
////            ElasticTimeUnit timeUnit = ElasticMetricTimeUtils.createOffsetTimeUnitFromNow(TimeUnit.DAYS, 1);
////
////            ElasticMetricDateRange dateRange = new ElasticMetricDateRange();
////            dateRange.setFrom(timeUnit.getFromDate());
////            dateRange.setTo(timeUnit.getToDate());
////
////            String[] indices      = generateIndicesByDateRange(dateRange, index);
////            String   indicesNames = logIndicesNames(indices);
////
////            IndicesOptions options = IndicesOptions.fromOptions(true, true, true, false);
////            SearchRequest searchRequest = new SearchRequest(indices);
////            searchRequest.indicesOptions(options);
////
////            // Queries should use the request cache if possible
////            searchRequest.requestCache(true);
////
////            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//////            sourceBuilder.explain(false); // don't show scoring
////            searchRequest.source(sourceBuilder.query(QueryBuilders.matchAllQuery()).explain(false));
////
////
////            List<String> dimensionKeys = request.getDimensionKeys();
////
////            if(dimensionKeys != null && dimensionKeys.isEmpty() == false) {
////                // It is important to distinct the requested dimension keys to prevent ElasticSearch aggregation builder error
////                // Cannot construct two aggregations with the same name
////                List<String> distinctDimKeys = dimensionKeys.stream().distinct().collect(Collectors.toList());
////                request.setDimensionKeys(distinctDimKeys);
////            }
////
////            DimensionsIndexManager manager = new DimensionsIndexManager(request);
////
////            // Set request query
////            manager.setFilters(sourceBuilder);
////            manager.setAggregations(sourceBuilder);
////
////            // Execute
////            StopWatch sw = new StopWatch();
////            sw.start();
////            SearchResponse searchResponse =
////                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
////            sw.stop();
////            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms",
////                                      indicesNames, DEFAULT_DOC_TYPE, sw.totalTime().millis()));
////
////            // Parse elastic search response
////            retVal = manager.parseResponse(searchResponse);
////
////        } catch(Exception ex) {
////            String errFormat = "Failed getting dimensions values from elastic search for request: %s";
////            String errMsg = String.format(errFormat, request.toString());
////            LOGGER.error(errMsg, ex);
////            throw new DalException(errMsg, ex);
////        }
////
////        LOGGER.info("Finished fetching dimensions values");
////
////        return retVal;
////    }
//
//    private static String logIndicesNames(String[] indices) {
//        StringBuilder sb = new StringBuilder();
//
//        if(indices != null && indices.length > 0) {
//            for(String idx : indices) {
//                sb.append(idx).append(", ");
//            }
//        }
//
//        String retVal = sb.toString();
//        LOGGER.debug(String.format("Starting to fetch metrics statistics from indices [%s]...", retVal));
//
//        return retVal;
//    }
//
//}
package com.spotinst.metrics.dal.services.elastic.infra;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import com.spotinst.metrics.dal.services.elastic.metric.ElasticMetricService;
import com.spotinst.metrics.dal.services.elastic.metric.IElasticMetricService;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ElasticSearchService extends BaseElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);

    private IElasticMetricService metricService;

    public ElasticSearchService() {
        super();
        this.restHighLevelClient = MetricsAppContext.getInstance().getElasticClient();
        this.metricService = new ElasticMetricService(restHighLevelClient);
    }

//    @Override
//    public ElasticMetricStatisticsResponse getAggregatedMetricsStatistics(
//            ElasticMetricStatisticsRequest esMetricStatisticsRequest)
//            throws DalException {
//        return metricService.getAggregatedMetricsStatistics(esMetricStatisticsRequest);
//    }

    // region Metric
    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest esMetricStatisticsRequest,
                                                           String index)
            throws DalException {
        return metricService.getMetricsStatistics(esMetricStatisticsRequest, index);
    }

    @Override
    public ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest esDimensionsValuesRequest,
                                                               String index)
            throws DalException {
        return metricService.getDimensionsValues(esDimensionsValuesRequest, index);
    }

    @Override
    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments, String index)
            throws DalException {
        return metricService.reportMetrics(esMetricDocuments, index);
    }
    //endregion
}