package com.spotinst.metrics.bl.index.custom;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.DIMENSIONS_PATH_PREFIX;

public class DimensionsIndexManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionsIndexManager.class);

    private RestHighLevelClient restHighLevelClient;
    private ElasticDimensionsValuesRequest request;

    public DimensionsIndexManager( ElasticDimensionsValuesRequest request) {;
        restHighLevelClient = MetricsAppContext.getInstance().getElasticClient();
        this.request = request;
    }

    public void setFilters(SearchSourceBuilder searchSourceBuilder) throws DalException {
        TermsQueryBuilder        namespaceQuery     = createNamespaceQuery();
        BoolQueryBuilder         dataOwnershipQuery = createDataOwnershipQuery();
        List<ExistsQueryBuilder> existsQueries      = createDimensionsExistsQueries();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.minimumShouldMatch("1");
        boolQuery.filter(namespaceQuery);
        existsQueries.forEach(boolQuery::should);
        boolQuery.filter(dataOwnershipQuery);

        searchSourceBuilder.query(boolQuery);
    }

    private List<ExistsQueryBuilder> createDimensionsExistsQueries() throws DalException {
        List<ExistsQueryBuilder> existQueries = new ArrayList<>();
        List<String> dimensionKeys = request.getDimensionKeys();

        if (dimensionKeys == null || dimensionKeys.isEmpty()) {
            String errMsg = "Dimensions keys are missing in the get metric dimensions request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        String format = "%s.%s";
        dimensionKeys.forEach(dimKey -> {
            String dimName = String.format(format, DIMENSIONS_PATH_PREFIX, dimKey);
            ExistsQueryBuilder existsQ = QueryBuilders.existsQuery(dimName);
            existQueries.add(existsQ);
        });

        return existQueries;
    }

    protected TermsQueryBuilder createNamespaceQuery() throws DalException {
        Set<String> namespaces   = new HashSet<>();
        String      reqNamespace = request.getNamespace();

        if (reqNamespace == null || reqNamespace.isEmpty()) {
            String errMsg = "Namespace is missing in the request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        namespaces.add(reqNamespace);
        return new TermsQueryBuilder("namespace", namespaces);
    }

    protected BoolQueryBuilder createDataOwnershipQuery() {
        String accountId = request.getAccountId();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.should(QueryBuilders.termQuery("accountId.keyword", accountId));
        return boolQuery;
    }

    public void setAggregations(SearchSourceBuilder searchSourceBuilder) throws DalException {
        List<String> dimensionKeys = request.getDimensionKeys();

        if (dimensionKeys != null && !dimensionKeys.isEmpty()) {
            for (String dimKey : dimensionKeys) {
                TermsAggregationBuilder aggregation = AggregationBuilders.terms(dimKey)
                                                                         .field(String.format("%s.%s", DIMENSIONS_PATH_PREFIX, dimKey))
                                                                         .size(1000);
                searchSourceBuilder.aggregation(aggregation);
            }
        } else {
            String errMsg = "Dimensions keys are missing in the get metric dimensions request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }
    }

    public ElasticDimensionsValuesResponse parseResponse(SearchResponse searchResponse) {
        // Implement the method to parse the search response
        return new ElasticDimensionsValuesResponse();
    }

    //TODO Tal: Pay attention to the Dal Exception here.. should I catch it or throw it?
    public ElasticDimensionsValuesResponse getDimensionsValues(String[] indices) throws IOException, DalException {
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        setFilters(searchSourceBuilder);
        setAggregations(searchSourceBuilder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return parseResponse(searchResponse);
    }
}