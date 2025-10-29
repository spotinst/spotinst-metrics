package com.spotinst.metrics.bl.index.custom;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.ElasticIndex;
import com.spotinst.metrics.commons.configuration.QueryConfig;
import com.spotinst.metrics.commons.enums.IndexSearchTypeEnum;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.NAMESPACE_FIELD_PATH;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.*;

public class DimensionsIndexManager {
    // region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionsIndexManager.class);

    private static final Integer DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT;

    private final ElasticDimensionsValuesRequest request;

    private IndexSearchTypeEnum searchType;
    //endregion

    // region Static initializer
    static {
        QueryConfig queryConfig = MetricsAppContext.getInstance().getConfiguration().getQueryConfig();
        DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT = queryConfig.getDimensionsValuesResultPerBucketLimit();
    }
    //endregion

    //region Constructor
    public DimensionsIndexManager(ElasticDimensionsValuesRequest request, String index) {
        this.request = request;
        initializeIndexSearchType(index);
    }
    //endregion


    //region Initialize Search Type
    private void initializeIndexSearchType(String index) {
        if (Objects.nonNull(index)) {
            ElasticIndex matchingIndex =
                    MetricsAppContext.getInstance().getConfiguration().getElastic().getIndexes().stream()
                                     .filter(i -> Objects.equals(index, i.getName())).findFirst().orElse(null);

            if (Objects.nonNull(matchingIndex)) {
                this.searchType = IndexSearchTypeEnum.fromName(matchingIndex.getSearchType());
            }
        }
        else {
            this.searchType = IndexSearchTypeEnum.EXACT_MATCH;
        }
    }
    //endregion

    // region Filters
    public void setFilters(SearchRequest.Builder srb) throws DalException {
        TermsQuery        namespaceQuery     = createNamespaceQuery();
        BoolQuery         dataOwnershipQuery = createDataOwnershipQuery();
        List<ExistsQuery> existsQueries      = createDimensionsExistsQueries();

        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool().minimumShouldMatch("1");

        // Use filter instead of query to benefit from elastic search cache and avoid document scoring
        boolQueryBuilder.filter(f -> f.terms(namespaceQuery));

        // Add exists queries on requests dimensions keys with OR operator between them
        boolQueryBuilder.should(existsQueries.stream().map(ExistsQuery::_toQuery).toList());

        // Append account ID on filter to enforce data ownership
        boolQueryBuilder.filter(b -> b.bool(dataOwnershipQuery));

        srb.query(f -> f.bool(boolQueryBuilder.build()));

        // Queries should use the request cache if possible
        srb.requestCache(true);

        // Do not return documents, we care about the aggregation content
        srb.size(0);
    }

    private List<ExistsQuery> createDimensionsExistsQueries() throws DalException {
        List<ExistsQuery> existQueries  = new ArrayList<>();
        List<String>      dimensionKeys = request.getDimensionKeys();

        if (CollectionUtils.isEmpty(dimensionKeys)) {
            String errMsg = "dimensions keys are missing in the get metric dimensions request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        dimensionKeys.forEach(dimKey -> {
            String dimensionField = String.format(DIMENSION_FIELD_PATH_FORMAT, dimKey);

            ExistsQuery existsQuery = QueryBuilders.exists().field(dimensionField).build();
            existQueries.add(existsQuery);
        });

        return existQueries;
    }

    protected BoolQuery createDataOwnershipQuery() {
        BoolQuery retVal;

        String accountIdValue = request.getAccountId();
        String accountIdField = ACCOUNT_ID_RAW_DIMENSION_NAME;

        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            accountIdField += METRIC_KEYWORD_SUFFIX;
        }

        Query accountIdQuery = QueryBuilders.term().field(accountIdField).value(accountIdValue).build()._toQuery();

        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }

    protected TermsQuery createNamespaceQuery() throws DalException {
        TermsQuery retVal;
        String     reqNamespace       = request.getNamespace();
        String     namespaceFieldPath = NAMESPACE_FIELD_PATH;

        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            namespaceFieldPath += METRIC_KEYWORD_SUFFIX;
        }

        if (StringUtils.isEmpty(reqNamespace)) {
            String errMsg = "[namespace] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        retVal = QueryBuilders.terms().field(namespaceFieldPath).terms(termsBuilder -> termsBuilder.value(
                Collections.singletonList(FieldValue.of(reqNamespace)))).build();

        return retVal;
    }
    // endregion

    // region Aggregations
    public void setAggregations(SearchRequest.Builder srb) throws DalException, IOException {
        List<String> dimensionKeys      = request.getDimensionKeys();
        String       dimensionFieldPath = DIMENSION_FIELD_PATH_FORMAT;

        if (BooleanUtils.isFalse(Objects.equals(searchType, IndexSearchTypeEnum.EXACT_MATCH))) {
            dimensionFieldPath += METRIC_KEYWORD_SUFFIX;
        }

        if (CollectionUtils.isNotEmpty(dimensionKeys)) {
            for (String dimensionKey : dimensionKeys) {
                if (StringUtils.isNotEmpty(dimensionKey)) {
                    String fieldPath = String.format(dimensionFieldPath, dimensionKey);
                    TermsAggregation dimAgg = AggregationBuilders.terms().minDocCount(1).shardMinDocCount(1L)
                                                                 .size(DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT)
                                                                 .field(fieldPath).build();

                    srb.aggregations(dimensionKey, dimAgg._toAggregation());
                }
            }
        }
    }
    // endregion

    // region Parse Response
    public ElasticDimensionsValuesResponse parseResponse(
            SearchResponse<ElasticDimensionsValuesResponse> searchResponse) throws DalException {
        ElasticDimensionsValuesResponse retVal = new ElasticDimensionsValuesResponse();

        List<String>                                  dimensionKeys = request.getDimensionKeys();
        HitsMetadata<ElasticDimensionsValuesResponse> hits          = searchResponse.hits();

        TotalHits totalHits = hits.total();
        LOGGER.debug(String.format("Search Response Total Hits: [%s]", totalHits));

        if (Objects.isNull(totalHits) || totalHits.value() == 0) {
            LOGGER.debug("No matches returned from elastic search, returning an empty response");
            return retVal;
        }

        Map<String, Set<Object>> resultMap      = new HashMap<>();
        Map<String, Aggregate>   allBucketsAggs = searchResponse.aggregations();

        // Create a Map structure with dimensions as keys and empty linked list as values
        dimensionKeys.forEach(d -> resultMap.put(d, new HashSet<>()));

        for (String dimensionKey : dimensionKeys) {
            Aggregate bucketAgg = allBucketsAggs.get(dimensionKey);

            if (Objects.nonNull(bucketAgg)) {
                Set<Object>             dimValuesResultSet = resultMap.get(dimensionKey);
                StringTermsAggregate    bucketsTerms       = bucketAgg.sterms();
                List<StringTermsBucket> buckets            = bucketsTerms.buckets().array();

                if (CollectionUtils.isNotEmpty(buckets)) {
                    buckets.forEach(t -> {
                        FieldValue dimensionValue = t.key();

                        if (dimensionValue != null) {
                            dimValuesResultSet.add(dimensionValue._get());
                        }
                    });
                }
            }
        }

        retVal = new ElasticDimensionsValuesResponse();
        retVal.setDimensions(resultMap);

        return retVal;
    }
    // endregion
}
