package com.spotinst.metrics.dal.services.elastic.metric;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.index.custom.DimensionsIndexManager;
import com.spotinst.metrics.bl.index.spotinst.RawIndexManager;
import com.spotinst.metrics.commons.utils.ElasticMetricTimeUtils;
import com.spotinst.metrics.commons.utils.ElasticTimeUnit;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricResultStatus;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricReportResponse;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static co.elastic.clients.elasticsearch._types.ExpandWildcard.Open;
import static co.elastic.clients.elasticsearch._types.SearchType.QueryThenFetch;
import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateDailyIndexName;
import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateIndicesByDateRange;

public class ElasticMetricService implements IElasticMetricService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticMetricService.class);

    private ElasticsearchClient elasticsearchClient;
    private BulkIngester<Void>  bulkIngester;

    //region Constructor
    public ElasticMetricService(ElasticsearchClient elasticsearchClient, BulkIngester bulkIngester) {
        this.elasticsearchClient = elasticsearchClient;
        this.bulkIngester = bulkIngester;
    }

    //endregion
    //region Private Methods
    private void indexBulk(List<String> documents, String index) throws DalException {
        String       idxName      = generateDailyIndexName(index);
        ObjectMapper objectMapper = new ObjectMapper();

        for (String document : documents) {
            try {
                LOGGER.debug("About to async bulk index the following document [{}] on elasticSearch on index [{}].",
                             document, idxName);

                Map<String, Object> jsonMap = objectMapper.readValue(document, new TypeReference<>() {
                });

                bulkIngester.add((op -> op.index(idx -> idx.index(idxName).document(jsonMap))));
            }
            catch (Exception e) {
                String errorMessage =
                        String.format("Failed to parse document to JSON. Document: %s. Exception: %s", document, e);

                LOGGER.error(errorMessage);
                throw new DalException(errorMessage, e);
            }
        }
    }
    //endregion

    //region Public Methods
    @Override
    public ElasticMetricReportResponse reportMetrics(List<ElasticMetricDocument> esMetricDocuments,
                                                     String index) throws DalException {
        ElasticMetricReportResponse retVal = new ElasticMetricReportResponse();

        if (CollectionUtils.isEmpty(esMetricDocuments)) {
            String errMsg = "Cannot report metrics to elastic search, no metrics to report";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }
        else {
            int docCount = esMetricDocuments.size();
            LOGGER.debug(String.format("Start reporting [%s] metrics data to elastic search...", docCount));

            List<String> documents = new ArrayList<>();

            for (ElasticMetricDocument doc : esMetricDocuments) {
                String json = JsonMapper.toJson(doc);

                if (StringUtils.isEmpty(json)) {
                    LOGGER.error("Cannot report metric to elastic search, failed serializing to JSON. Doc: {}",
                                 doc.toString());
                }
                else {
                    documents.add(json);
                }
            }

            indexBulk(documents, index);

            //            if (Objects.nonNull(bulkRequest)) {
            //                try {
            //                    bulkResponse = elasticsearchClient.bulk(bulkRequest);
            //                }
            //                catch (Exception e) {
            //                    String errorMessage =
            //                            String.format("Failed to report metrics to elastic search. Request: %s. Exception: %s",
            //                                          bulkRequest, e);
            //                    LOGGER.error(errorMessage);
            //                    throw new DalException(errorMessage, e);
            //                }
            //            }
            //
            //            if (bulkResponse.errors()) {
            //                //todo tal oyar -  Should we go through all the items and check for error reason and print those?
            //                // will provide visibility but can be excessive.
            //                String errorMessage = "Failed to report metrics to elastic search.";
            //                LOGGER.error(errorMessage);
            //                throw new DalException(errorMessage);
            //            }
            // else {

            LOGGER.debug(String.format("Finished bulk indexing [%s] metrics to elastic search", documents.size()));

            ElasticMetricResultStatus status = new ElasticMetricResultStatus();
            status.setCode(200);
            status.setName("OK");
            retVal.setStatus(status);
        }

        return retVal;
    }

    @Override
    public ElasticDimensionsValuesResponse getDimensionsValues(ElasticDimensionsValuesRequest request,
                                                               String index) throws DalException {

        ElasticDimensionsValuesResponse retVal;

        try {
            ElasticTimeUnit timeUnit = ElasticMetricTimeUtils.createOffsetTimeUnitFromNow(TimeUnit.DAYS, 1);

            ElasticMetricDateRange dateRange = new ElasticMetricDateRange();
            dateRange.setFrom(timeUnit.getFromDate());
            dateRange.setTo(timeUnit.getToDate());

            List<String> indices      = generateIndicesByDateRange(dateRange, index);
            String       indicesNames = logIndicesNames(indices);

            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
            searchRequestBuilder.index(indices).ignoreUnavailable(true).allowNoIndices(true).expandWildcards(Open)
                                .searchType(QueryThenFetch).explain(false);

            List<String> dimensionKeys = request.getDimensionKeys();
            if (dimensionKeys != null && dimensionKeys.isEmpty() == false) {
                // It is important to distinct the requested dimension keys to prevent ElasticSearch aggregation builder error
                // Cannot construct two aggregations with the same name
                List<String> distinctDimKeys = dimensionKeys.stream().distinct().collect(Collectors.toList());
                request.setDimensionKeys(distinctDimKeys);
            }

            DimensionsIndexManager manager = new DimensionsIndexManager(request);

            // Set request query
            manager.setFilters(searchRequestBuilder);
            manager.setAggregations(searchRequestBuilder);
            SearchRequest searchRequest = searchRequestBuilder.build();

            // Execute
            StopWatch sw = new StopWatch();
            sw.start();

            SearchResponse<ElasticDimensionsValuesResponse> searchResponse =
                    elasticsearchClient.search(searchRequest, ElasticDimensionsValuesResponse.class);

            sw.stop();
            LOGGER.info(String.format("Elastic search query on indices [%s] took [%s]ms", indicesNames, sw.getTime()));

            // Parse elastic search response
            retVal = manager.parseResponse(searchResponse);

        }
        catch (Exception ex) {
            String errorMessage = String.format("Failed getting dimensions values from elastic search for request: %s",
                                                request.toString());
            LOGGER.error(errorMessage, ex);
            throw new DalException(errorMessage, ex);
        }

        LOGGER.info("Finished fetching dimensions values");

        return retVal;
    }

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
    //endregion

    @Override
    public ElasticMetricStatisticsResponse getMetricsStatistics(ElasticMetricStatisticsRequest request,
                                                                String index) throws DalException {
        ElasticMetricStatisticsResponse retVal;
        try {
            ElasticMetricDateRange dateRange    = request.getDateRange();
            List<String>           indices      = generateIndicesByDateRange(dateRange, index);
            String                 indicesNames = logIndicesNames(indices);

            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
            searchRequestBuilder.ignoreUnavailable(true).allowNoIndices(true).expandWildcards(Open)
                                .searchType(QueryThenFetch).explain(false).index(indices);

            RawIndexManager manager = new RawIndexManager(request);

            // Set request query
            manager.setFilters(searchRequestBuilder);
            manager.setAggregations(searchRequestBuilder);

            //            ExistsQuery existsQuery = new ExistsQuery.Builder().field("accountId").build();
            //           SearchRequest searchRequest2 = new SearchRequest.Builder().index("opt_metrics-2025.05.07").query(q-> q.match(m-> m.field()).exists(existsQuery)).build();

            SearchRequest searchRequest = searchRequestBuilder.build();

            long startTime = System.currentTimeMillis();

            SearchResponse<ElasticMetricStatisticsResponse> searchResponse =
                    elasticsearchClient.search(searchRequest, ElasticMetricStatisticsResponse.class);

            long endTime = System.currentTimeMillis();

            LOGGER.info(String.format("Elastic search query on indices [%s] took [%s]ms", indicesNames,
                                      (endTime - startTime)));

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

    //todo tal oyar - only being used in the LB resource.

    //    @Override
    //    public ElasticMetricStatisticsResponse getAggregatedMetricsStatistics(ElasticMetricStatisticsRequest request)
    //            throws DalException {
    //        ElasticMetricStatisticsResponse retVal;
    //        Boolean                    useAggregationIndex = true;
    //
    //        try {
    //            ElasticMetricDateRange dateRange = request.getDateRange();
    //            List<String> indices = EsIndexNamingUtils.generateIndicesByDateRange(dateRange, null, useAggregationIndex);
    //            String indicesNames = logIndicesNames(indices);
    //
    //
    //            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
    //            searchRequestBuilder.ignoreUnavailable(true).allowNoIndices(true).expandWildcards(Open)
    //                                .searchType(QueryThenFetch).explain(false).index(indices);
    //
    //
    //            AggregationIndexManager manager = new AggregationIndexManager(request);
    //
    //            // Set request query
    //            manager.setFilters(srb);
    //            manager.setAggregations(srb);
    //
    //            // Execute
    //            StopWatch sw = new StopWatch();
    //            sw.start();
    //            SearchResponse searchResponse = srb.get();
    //            sw.stop();
    //            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms",
    //                                      indicesNames, DEFAULT_DOC_TYPE, sw.totalTime().millis()));
    //
    //            retVal = manager.parseResponse(searchResponse);
    //
    //        } catch(Exception ex) {
    //            String errorMessage = String.format("Failed getting metric statistics from elastic search for request: %s",
    //                                                request.toString());
    //            LOGGER.error(errorMessage, ex);
    //            throw new DalException(errorMessage, ex);
    //        }
    //
    //        LOGGER.debug("Finished fetching metrics statistics by request filter");
    //
    //        return retVal;
    //    }


    private String logIndicesNames(List<String> indices) {
        StringBuilder sb = new StringBuilder();

        if (indices != null && indices.size() > 0) {
            for (String idx : indices) {
                sb.append(idx).append(", ");
            }
        }

        String retVal = sb.toString();

        LOGGER.debug(String.format("Starting to fetch metrics statistics from indices [%s]...", retVal));

        return retVal;
    }
}