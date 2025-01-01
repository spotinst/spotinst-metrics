package com.spotinst.metrics.dal.services.elastic.metric;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class ElasticSearchValidator {

    private RestHighLevelClient client;

    public ElasticSearchValidator(RestHighLevelClient client) {
        this.client = client;
    }

    public boolean checkIndexExists(String index) {
        try {
            GetIndexRequest request = new GetIndexRequest().indices(index);
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            return response.getIndices().length > 0;
        } catch (ElasticsearchStatusException e) {
//            LOGGER.error("ElasticsearchStatusException: Unable to parse response body", e);
            return false;
        } catch (Exception e) {
//            LOGGER.error("Exception: Error checking if index exists", e);
            return false;
        }
    }

    public boolean checkDocumentInRange(String index, String from, String to) throws Exception {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.rangeQuery("timestamp").from(from).to(to));
        SearchRequest searchRequest = new SearchRequest(index).source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getTotalHits().value > 0;
    }

    public boolean checkFieldValues(String index, String namespace, String groupId, String accountId) throws Exception {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.boolQuery()
                                         .must(QueryBuilders.termQuery("namespace.keyword", namespace))
                                         .must(QueryBuilders.termQuery("dimensions.group_id", groupId))
                                         .must(QueryBuilders.termQuery("accountId", accountId)));
        SearchRequest searchRequest = new SearchRequest(index).source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getTotalHits().value > 0;
    }

    public boolean checkFieldExists(String index, String field) throws Exception {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.existsQuery(field));
        SearchRequest searchRequest = new SearchRequest(index).source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getTotalHits().value > 0;
    }

    public boolean checkIndexMapping(String index) throws Exception {
        // Implement index mapping check logic here
        return true;
    }

    public boolean checkDocumentVisibility(String index) throws Exception {
        // Implement document visibility check logic here
        return true;
    }

    public boolean checkQueryExecution(String index) throws Exception {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchRequest searchRequest = new SearchRequest(index).source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getTotalHits().value > 0;
    }
}