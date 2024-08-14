package com.spotinst.metrics.dal.dao.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.index.spotinst.RawIndexManager;
import com.spotinst.metrics.dal.models.elastic.BaseIdentifiableElasticEntity;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricAggregations;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.common.ElasticSearchRequest;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import jakarta.validation.Valid;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static com.spotinst.metrics.commons.utils.EsIndexNamingUtils.generateIndicesByDateRange;

public abstract class BaseElasticDAO {
    //region Members
    protected static final Integer         DEFAULT_RESPONSE_HITS_LIMIT       = 10000;
    protected static final Integer         DEFAULT_SCROLL_KEEP_ALIVE_SECONDS = 30;
    private                TransportClient transportClient;

    private static final ObjectMapper objectMapper     = new ObjectMapper();
    private static final String       DEFAULT_DOC_TYPE = "balancer";

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticDAO.class);
    //endregion

    //region Public Methods
    public <T> boolean create(List<T> entities) throws IOException {
        boolean retVal = true;

        BulkResponse responses = createBulkResponse(entities);

        if (responses.hasFailures()) {
            retVal = false;
            LOGGER.error("Failed to report entities. Errors: {}", responses.buildFailureMessage());
        }

        return retVal;
    }

    //    public <T> boolean bulkUpdate(List<ElasticUpdateRequest<T>> updateRequests) throws IOException {
    //        boolean retVal = true;
    //
    //        BulkResponse responses = updateBulkResponse(updateRequests);
    //
    //        if (responses.hasFailures()) {
    //            retVal = false;
    //            LOGGER.error("Failed to update entities. Errors: {}", responses.buildFailureMessage());
    //        }
    //
    //        return retVal;
    //    }
    //
    //    public <T> int createAffectedRows(List<T> entities) throws IOException {
    //        int          retVal;
    //        int          countFailures = 0;
    //        BulkResponse responses     = createBulkResponse(entities);
    //
    //        if (responses.hasFailures()) {
    //            LOGGER.error("Failed to report entities. Errors: {}", responses.buildFailureMessage());
    //
    //            for (BulkItemResponse response : responses) {
    //                if (response.isFailed()) {
    //                    countFailures++;
    //                }
    //            }
    //        }
    //
    //        retVal = responses.getItems().length - countFailures;
    //
    //        return retVal;
    //    }
    //
    public <T> BulkResponse createBulkResponse(List<T> entities) throws IOException {
        BulkResponse retVal;

        RestHighLevelClient elasticClient  = MetricsAppContext.getInstance().getElasticClient();
        String              indexName      = getBaseIndexName();
        String              indexForCreate = indexName;
        //        String              indexSuffix    = getIndexSuffix();
        String indexSuffix = null;

        if (indexSuffix != null) {
            indexForCreate = indexName + "-" + indexSuffix;
        }

        BulkRequest request = new BulkRequest();

        for (T entity : entities) {
            String source = JsonMapper.toJson(entity);
            request.add(new IndexRequest(indexForCreate).source(source, XContentType.JSON));
        }

        LOGGER.info("Bulk creating {} entities in index:{}", entities.size(), indexForCreate);

        BulkResponse responses = elasticClient.bulk(request, RequestOptions.DEFAULT);

        retVal = responses;

        return retVal;
    }
    //
    //    public <T> BulkResponse updateBulkResponse(List<ElasticUpdateRequest<T>> updateRequests) throws IOException {
    //        BulkResponse retVal;
    //
    //        RestHighLevelClient elasticClient  = MarketStatisticsAppContext.getInstance().getElasticClient();
    //        String              indexName      = getBaseIndexName();
    //        String              indexForCreate = indexName;
    //        String              indexSuffix    = getIndexSuffix();
    //
    //        if (indexSuffix != null) {
    //            indexForCreate = indexName + "-" + indexSuffix;
    //        }
    //
    //        BulkRequest bulkRequest = new BulkRequest();
    //
    //        for (ElasticUpdateRequest<T> updateRequest : updateRequests) {
    //            String object = JsonMapper.toJson(updateRequest.getEntity());
    //            bulkRequest.add(new UpdateRequest(indexForCreate, updateRequest.getId()).doc(object, XContentType.JSON));
    //        }
    //
    //        LOGGER.info("Bulk updating {} entities in index:{}", updateRequests.size(), indexForCreate);
    //
    //        BulkResponse responses = elasticClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    //
    //        retVal = responses;
    //
    //        return retVal;
    //    }
    //
    //    public <T> boolean update(String id, T entity) throws IOException {
    //        boolean retVal = false;
    //
    //        RestHighLevelClient elasticClient = MarketStatisticsAppContext.getInstance().getElasticClient();
    //
    //        String indexName   = getBaseIndexName();
    //        String indexSuffix = getIndexSuffix();
    //
    //        if (indexSuffix != null) {
    //            indexName = indexName + "-" + indexSuffix;
    //        }
    //
    //        UpdateRequest request    = new UpdateRequest(indexName, id);
    //        String        entityJson = JsonMapper.toJson(entity);
    //        request.doc(entityJson, XContentType.JSON);
    //
    //        UpdateResponse response = elasticClient.update(request, RequestOptions.DEFAULT);
    //
    //        //todo handle errors better
    //        if (response.getResult() == DocWriteResponse.Result.UPDATED) {
    //            retVal = true;
    //        }
    //
    //        return retVal;
    //    }
    //endregion

    // region Protected Methods
    //    @Override
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
            SearchResponse searchResponse =
                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);

            RawIndexManager manager = new RawIndexManager(request);
            //
            //
            // Set request query
            manager.setFilters(sourceBuilder);
            manager.setAggregations(sourceBuilder);
//            SearchResponse searchResponse =
//                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
            searchResponse =
                    MetricsAppContext.getInstance().getElasticClient().search(searchRequest, RequestOptions.DEFAULT);
            //
            // Execute
            StopWatch sw = new StopWatch();
            sw.start();
//            SearchResponse searchResponse = sourceBuilder.get();
            sw.stop();
//            LOGGER.info(String.format("Elastic search query on indices [%s] on type [%s] took [%s]ms", indicesNames,
//                                      DEFAULT_DOC_TYPE, sw.totalTime().millis()));

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


    //    public List<ElasticMetricStatistics> getMetricsStatistics(ElasticMetricStatisticsRequest statisticRequest) throws IOException {
    ////        RestHighLevelClient client = MetricsAppContext.getInstance().getElasticClient();
    //        ElasticSearchRequest request = new ElasticSearchRequest();
    //
    //        // Create a BoolQueryBuilder which will be used to construct the query
    ////        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    //
    //        if (statisticRequest != null) {
    //            Map<String, Object> matches = new HashMap<>();
    //
    //            if (statisticRequest.getAccountId() != null) {
    //                matches.put("accountId", statisticRequest.getAccountId());
    //            }
    //            if (statisticRequest.getNamespace() != null) {
    //                matches.put("namespace", statisticRequest.getNamespace());
    //            }
    //            if (statisticRequest.getMetricName() != null) {
    //                matches.put("metricName", statisticRequest.getMetricName());
    //            }
    //            if (statisticRequest.getStatistic() != null) {
    //                matches.put("statistic", statisticRequest.getStatistic());
    //            }
    //            if (matches.size() > 0) {
    //                ElasticSearchRequest.Filter       filter  = new ElasticSearchRequest.Filter(matches, null);
    //                List<ElasticSearchRequest.Filter> filters = Collections.singletonList(filter);
    //                request.setFilters(filters);
    //            }
    //        }
    //
    //        List<ElasticMetricStatistics> retVal = search(request, ElasticMetricStatistics.class);
    //
    //        return retVal;
    ////        // Add conditions to the query based on the request
    ////        boolQuery.must(QueryBuilders.matchQuery("accountId", request.getAccountId()));
    ////        boolQuery.must(QueryBuilders.matchQuery("namespace", request.getNamespace()));
    ////        boolQuery.must(QueryBuilders.matchQuery("metricName", request.getMetricName()));
    //
    //        // TODO: Add more conditions based on your request object
    //
    ////        // Create a SearchSourceBuilder and set the query
    ////        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    ////        searchSourceBuilder.query(boolQuery);
    ////
    ////        // Create the SearchRequest and set the indices
    ////        SearchRequest searchRequest = new SearchRequest("your_index_name");
    ////        searchRequest.source(searchSourceBuilder);
    ////
    ////        // Perform the search operation
    ////        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    ////
    ////        // Process the SearchResponse
    ////        ElasticMetricStatisticsResponse response = new ElasticMetricStatisticsResponse();
    ////        for (SearchHit hit : searchResponse.getHits().getHits()) {
    ////            // TODO: Extract the required information from the hit and add it to the response
    ////        }
    //
    ////        return response;
    //    }


    protected <T> List<T> search(@Valid ElasticSearchRequest request, Class<T> sourceClass) throws IOException {
        RestHighLevelClient elasticClient = MetricsAppContext.getInstance().getElasticClient();
        SearchRequest       searchRequest = buildSearchRequest(request, sourceClass);

        SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits     hits           = searchResponse.getHits();

        List<T> retVal = mapHitsToEntities(hits, sourceClass);
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

    //
    //    protected <T> List<T> searchWithScroll(@Valid ElasticSearchRequest request,
    //                                           Class<T> sourceClass) throws IOException {
    //        RestHighLevelClient elasticClient = MarketStatisticsAppContext.getInstance().getElasticClient();
    //
    //        SearchRequest searchRequest = buildSearchRequest(request, sourceClass);
    //        searchRequest.scroll(TimeValue.timeValueSeconds(DEFAULT_SCROLL_KEEP_ALIVE_SECONDS));
    //
    //        /*
    //          Scroll requests have optimizations that make them faster when the sort order is _doc.
    //          If you want to iterate over all documents regardless of the order, this is the most efficient option
    //         */
    //        if (CollectionUtils.isEmpty(searchRequest.source().sorts())) {
    //            searchRequest.source().sort("_doc");
    //        }
    //
    //        int            requestedPageSize = searchRequest.source().size();
    //        SearchResponse response          = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
    //
    //        List<T> retVal = mapHitsToEntities(response.getHits(), sourceClass);
    //        scrollForResults(response, requestedPageSize, retVal, sourceClass);
    //
    //        return retVal;
    //    }
    //
    //    protected AggregationBuilder buildFunctionalNestedAggregation(String[] dimensions, int depth,
    //                                                                  AggregationBuilder functionalSubAggregation) {
    //        if (dimensions.length == (depth + 1)) {
    //            return AggregationBuilders.terms(dimensions[depth]).field(dimensions[depth])
    //                                      .size(DEFAULT_RESPONSE_HITS_LIMIT).subAggregation(functionalSubAggregation);
    //        }
    //
    //        return AggregationBuilders.terms(dimensions[depth]).field(dimensions[depth]).subAggregation(
    //                                          buildFunctionalNestedAggregation(dimensions, depth + 1, functionalSubAggregation))
    //                                  .size(DEFAULT_RESPONSE_HITS_LIMIT);
    //    }
    //
    //    protected AggregationBuilder nestDistinctAggregations(String[] dimensions, int depth) {
    //        if (dimensions.length == (depth + 1)) {
    //            return AggregationBuilders.terms(dimensions[depth]).field(dimensions[depth])
    //                                      .size(DEFAULT_RESPONSE_HITS_LIMIT)
    //                                      .order(Arrays.asList(BucketOrder.count(false), BucketOrder.key(true)));
    //        }
    //
    //        return AggregationBuilders.terms(dimensions[depth]).field(dimensions[depth])
    //                                  .subAggregation(nestDistinctAggregations(dimensions, depth + 1))
    //                                  .size(DEFAULT_RESPONSE_HITS_LIMIT);
    //    }
    //
    protected BoolQueryBuilder buildQuery(@Valid ElasticSearchRequest request) {
        BoolQueryBuilder retVal = QueryBuilders.boolQuery();

        List<ElasticSearchRequest.Filter> filterList = request.getFilters();

        for (ElasticSearchRequest.Filter filter : filterList) {
            BoolQueryBuilder    boolQueryBuilder = QueryBuilders.boolQuery();
            Map<String, Object> matches          = filter.getMatches();

            for (Map.Entry<String, Object> entry : matches.entrySet()) {
                String name  = entry.getKey();
                Object value = entry.getValue();

                boolQueryBuilder.filter(QueryBuilders.matchQuery(name, value).operator(Operator.AND));
            }

            for (ElasticSearchRequest.Range range : filter.getRanges()) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(range.getName()).gte(range.getFrom());

                if (range.getTo() != null) {
                    rangeQueryBuilder.lte(range.getTo());
                }

                boolQueryBuilder.filter(rangeQueryBuilder);
            }

            retVal.should(boolQueryBuilder);
        }
        return retVal;
    }
    //
    //    /**
    //     * @param searchRequest
    //     * @param bucketPageSize
    //     * @param compositeAggregationName
    //     * @param aggregationBucketsAsList
    //     * @param <T>
    //     * @return Aggregation result (without applied function)-> Similar to mysql select field1,field2,field3 from table group by field1,field2,field3
    //     * @throws IOException
    //     */
    //    protected <T> List<T> searchPaginatedCompositeAggregationKey(SearchRequest searchRequest, int bucketPageSize,
    //                                                                 String compositeAggregationName,
    //                                                                 Function<Map<String, Object>, T> aggregationBucketsAsList) throws IOException {
    //        List<T> retVal = new ArrayList<>();
    //
    //        Map<String, AggregationBuilder> aggregationByName = getAggregationsFromSearchRequest(searchRequest);
    //
    //        RestHighLevelClient elasticClient = MarketStatisticsAppContext.getInstance().getElasticClient();
    //
    //        boolean shouldSearchForMorePages;
    //
    //        do {
    //            SearchResponse  searchResponse             = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
    //            ParsedComposite compositeAggregationResult = searchResponse.getAggregations().get(compositeAggregationName);
    //
    //            List<ParsedComposite.ParsedBucket> buckets = compositeAggregationResult.getBuckets();
    //
    //            List<T> aggregationsObjList = aggregationBucketsAsList(buckets, aggregationBucketsAsList);
    //            retVal.addAll(aggregationsObjList);
    //
    //            shouldSearchForMorePages = buckets.size() == bucketPageSize;
    //
    //            if (shouldSearchForMorePages) {
    //                Map<String, Object> afterKey = compositeAggregationResult.afterKey();
    //                setAfterKeyForCompositeAggregation(aggregationByName, compositeAggregationName, afterKey);
    //            }
    //        }
    //        while (shouldSearchForMorePages);
    //
    //        return retVal;
    //    }
    //
    //    protected <K, V> Map<K, V> searchPaginatedCompositeAggregation(SearchRequest searchRequest, int bucketPageSize,
    //                                                                   String compositeAggregationName,
    //                                                                   Function<Map<String, Object>, K> keyFromBucketKeyMapper,
    //                                                                   Function<Aggregations, V> valueFromBucketAggregationsMapper) throws IOException {
    //        Map<K, V> retVal = new HashMap<>();
    //
    //        Map<String, AggregationBuilder> aggregationByName = getAggregationsFromSearchRequest(searchRequest);
    //
    //        RestHighLevelClient elasticClient = MarketStatisticsAppContext.getInstance().getElasticClient();
    //
    //        boolean shouldSearchForMorePages = false;
    //
    //        do {
    //            SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
    //            Aggregations   aggregations   = searchResponse.getAggregations();
    //
    //            if (aggregations != null) {
    //                ParsedComposite compositeAggregationResult = aggregations.get(compositeAggregationName);
    //
    //                if (compositeAggregationResult != null) {
    //                    List<ParsedComposite.ParsedBucket> buckets = compositeAggregationResult.getBuckets();
    //                    Map<K, V> bucketsAsMap =
    //                            aggregationBucketsAsMap(buckets, keyFromBucketKeyMapper, valueFromBucketAggregationsMapper);
    //                    retVal.putAll(bucketsAsMap);
    //
    //                    shouldSearchForMorePages = buckets.size() == bucketPageSize;
    //
    //                    if (shouldSearchForMorePages) {
    //                        Map<String, Object> afterKey = compositeAggregationResult.afterKey();
    //                        setAfterKeyForCompositeAggregation(aggregationByName, compositeAggregationName, afterKey);
    //                    }
    //                }
    //            }
    //        }
    //        while (shouldSearchForMorePages);
    //
    //        return retVal;
    //    }
    //endregion

    //region Abstract Methods
    protected abstract String getIndexSuffix();

    protected abstract String getBaseIndexName();
    //endregion

    //region Private Methods
    private <T> SearchRequest buildSearchRequest(@Valid ElasticSearchRequest request, Class<T> sourceClass) {
        BoolQueryBuilder queryBuilder = buildQuery(request);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);

        Field[]      declaredFields = sourceClass.getDeclaredFields();
        List<String> fieldNames     = new ArrayList<>();

        for (Field field : declaredFields) {
            fieldNames.add(field.getName());
        }
        String[] includeFields = fieldNames.toArray(new String[0]);
        sourceBuilder.fetchSource(includeFields, null);

        if (request.getLimit() != null) {
            sourceBuilder.size(request.getLimit());
        }
        else {
            sourceBuilder.size(DEFAULT_RESPONSE_HITS_LIMIT);
        }

        String        indexName = getBaseIndexName();
        SearchRequest retVal    = new SearchRequest(indexName + "*");
        retVal.source(sourceBuilder);

        return retVal;
    }

    private <T> List<T> mapHitsToEntities(SearchHits hits, Class<T> entityClass) throws JsonProcessingException {
        SearchHit[] searchHits = hits.getHits();
        List<T>     retVal     = new ArrayList<>(searchHits.length);

        boolean shouldEnrichWithDocumentId = BaseIdentifiableElasticEntity.class.isAssignableFrom(entityClass);

        for (SearchHit hit : searchHits) {
            T entity = objectMapper.readValue(hit.getSourceAsString(), entityClass);

            if (shouldEnrichWithDocumentId) {
                ((BaseIdentifiableElasticEntity) entity).setId(hit.getId());
            }

            retVal.add(entity);
        }

        return retVal;
    }
    //
    //    private <T> void scrollForResults(SearchResponse initialSearchResponse, int requestedPageSize, List<T> results,
    //                                      Class<T> sourceClass) throws IOException {
    //        RestHighLevelClient elasticClient = MarketStatisticsAppContext.getInstance().getElasticClient();
    //
    //        Set<String> scrollIds = new HashSet<>();
    //        String      scrollId  = initialSearchResponse.getScrollId();
    //        scrollIds.add(scrollId);
    //
    //        int foundResult = initialSearchResponse.getHits().getHits().length;
    //
    //        boolean hasMoreEntitiesToScroll = foundResult == requestedPageSize;
    //
    //        while (hasMoreEntitiesToScroll) {
    //            SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
    //            searchScrollRequest.scrollId(scrollId);
    //            searchScrollRequest.scroll(TimeValue.timeValueSeconds(DEFAULT_SCROLL_KEEP_ALIVE_SECONDS));
    //
    //            SearchResponse scrollResponse = elasticClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
    //
    //            List<T> searchResults = mapHitsToEntities(scrollResponse.getHits(), sourceClass);
    //            results.addAll(searchResults);
    //
    //            int foundEntitiesOnPage = scrollResponse.getHits().getHits().length;
    //            hasMoreEntitiesToScroll = foundEntitiesOnPage == requestedPageSize;
    //
    //            scrollId = scrollResponse.getScrollId();
    //            if (scrollId != null) {
    //                scrollIds.add(scrollId);
    //            }
    //        }
    //
    //        clearScrollsAsync(scrollIds);
    //    }
    //
    //    private void clearScrollsAsync(Collection<String> scrollIds) {
    //        RestHighLevelClient elasticClient      = MarketStatisticsAppContext.getInstance().getElasticClient();
    //        ClearScrollRequest  clearScrollRequest = new ClearScrollRequest();
    //
    //        scrollIds.forEach(clearScrollRequest::addScrollId);
    //
    //        elasticClient.clearScrollAsync(clearScrollRequest, RequestOptions.DEFAULT,
    //                                       new ActionListener<ClearScrollResponse>() {
    //                                           @Override
    //                                           public void onResponse(ClearScrollResponse clearScrollResponse) {
    //
    //                                           }
    //
    //                                           @Override
    //                                           public void onFailure(Exception e) {
    //                                               LOGGER.warn("Failed to clear scroll in ES. Errors: {}", e.getMessage());
    //                                           }
    //                                       });
    //    }
    //
    //    private Map<String, AggregationBuilder> getAggregationsFromSearchRequest(SearchRequest searchRequest) {
    //        Map<String, AggregationBuilder> retVal = searchRequest.source().aggregations().getAggregatorFactories().stream()
    //                                                              .collect(Collectors.toMap(AggregationBuilder::getName,
    //                                                                                        Function.identity()));
    //
    //        return retVal;
    //    }
    //
    //    private void setAfterKeyForCompositeAggregation(Map<String, AggregationBuilder> aggregationByName,
    //                                                    String compositeAggregationName, Map<String, Object> afterKey) {
    //        AggregationBuilder aggregation = aggregationByName.get(compositeAggregationName);
    //
    //        if (aggregation != null) {
    //            ((CompositeAggregationBuilder) aggregation).aggregateAfter(afterKey);
    //        }
    //        else {
    //            throw new IllegalArgumentException("Cannot find aggregation with name " + compositeAggregationName);
    //        }
    //    }
    //
    //    private <K, V> Map<K, V> aggregationBucketsAsMap(List<ParsedComposite.ParsedBucket> buckets,
    //                                                     Function<Map<String, Object>, K> keyFromBucketKeyMapper,
    //                                                     Function<Aggregations, V> valueFromBucketAggregationsMapper) {
    //        Map<K, V> retVal = new HashMap<>(buckets.size());
    //
    //        for (ParsedComposite.ParsedBucket bucket : buckets) {
    //            K key   = keyFromBucketKeyMapper.apply(bucket.getKey());
    //            V value = valueFromBucketAggregationsMapper.apply(bucket.getAggregations());
    //
    //            retVal.put(key, value);
    //        }
    //
    //        return retVal;
    //    }
    //
    //    private <T> List<T> aggregationBucketsAsList(List<ParsedComposite.ParsedBucket> buckets,
    //                                                 Function<Map<String, Object>, T> aggregationFromBucketKeyMapper) {
    //        List<T> retVal = new ArrayList<>(buckets.size());
    //
    //        for (ParsedComposite.ParsedBucket bucket : buckets) {
    //            T aggregation = aggregationFromBucketKeyMapper.apply(bucket.getKey());
    //            retVal.add(aggregation);
    //        }
    //
    //        return retVal;
    //    }
    //endregion
}
