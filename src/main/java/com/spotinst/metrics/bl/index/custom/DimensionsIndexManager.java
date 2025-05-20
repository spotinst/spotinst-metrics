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
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
import com.spotinst.metrics.commons.configuration.QueryConfig;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.ACCOUNT_ID_RAW_DIMENSION_NAME;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.DIMENSIONS_PATH_PREFIX;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.NAMESPACE_FIELD_PATH;
import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.OPTIMIZED_INDEX_PREFIX;
import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_KEYWORD_SUFFIX;

/**
 * Created by zachi.nachshon on 4/24/17.
 */
public class DimensionsIndexManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionsIndexManager.class);

    // If service configured to read from optimized index, field paths should exclude the use of 'keyword'
    private static Boolean IS_READ_FROM_OPTIMIZED_INDEX;
    private static Integer DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT;

    private ElasticDimensionsValuesRequest request;

    static {
        ElasticConfig config = MetricsAppContext.getInstance().getConfiguration().getElastic();

        // By default, service shouldn't read from optimized index (may be changed later on)
        IS_READ_FROM_OPTIMIZED_INDEX = false;

        IndexNamePatterns indexNamePatterns = config.getIndexNamePatterns();
        if (indexNamePatterns != null) {
            String readPattern = indexNamePatterns.getReadPattern();
            if (readPattern != null) {
                String idxPrefix = StringUtils.isEmpty(readPattern) ? "" : readPattern;
                IS_READ_FROM_OPTIMIZED_INDEX = idxPrefix.startsWith(OPTIMIZED_INDEX_PREFIX);
            }
        }

        QueryConfig queryConfig = MetricsAppContext.getInstance().getConfiguration().getQueryConfig();
        DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT = queryConfig.getDimensionsValuesResultPerBucketLimit();
    }

    public DimensionsIndexManager(ElasticDimensionsValuesRequest request) {
        this.request = request;
    }

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
        if (dataOwnershipQuery != null) {
            boolQueryBuilder.filter(b -> b.bool(dataOwnershipQuery));
        }

        srb.query(f -> f.bool(boolQueryBuilder.build()));

        // Queries should use the request cache if possible
        srb.requestCache(true);

        // Do not return documents, we care about the aggregation content
        srb.size(0);
    }

    private List<ExistsQuery> createDimensionsExistsQueries() throws DalException {
        List<ExistsQuery> existQueries  = new ArrayList<>();
        List<String>      dimensionKeys = request.getDimensionKeys();

        if (dimensionKeys == null || dimensionKeys.isEmpty()) {
            String errMsg = "dimensions keys are missing in the get metric dimensions request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        String format = "%s.%s";
        dimensionKeys.forEach(dimKey -> {
            String      dimensionField = String.format(format, DIMENSIONS_PATH_PREFIX, dimKey);
            ExistsQuery existsQuery    = QueryBuilders.exists().field(dimensionField).build();
            existQueries.add(existsQuery);
        });

        return existQueries;
    }

    protected BoolQuery createDataOwnershipQuery() {
        BoolQuery retVal;

        String accountId     = request.getAccountId();
        String accountIdPath = ACCOUNT_ID_RAW_DIMENSION_NAME;

        //todo tal - probably need to always add the keyword suffix from now on
        // it was only added when reading from non-optimized index - but seems like it should be added always now.

        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
        if (IS_READ_FROM_OPTIMIZED_INDEX) {
            accountIdPath += METRIC_KEYWORD_SUFFIX;
        }

        Query accountIdQuery = QueryBuilders.term().field(accountIdPath).value(accountId).build()._toQuery();

        retVal = QueryBuilders.bool().should(accountIdQuery).build();

        return retVal;
    }

    protected TermsQuery createNamespaceQuery() throws DalException {
        TermsQuery retVal;
        String     reqNamespace = request.getNamespace();

        if (StringUtils.isEmpty(reqNamespace)) {
            String errMsg = "[namespace] is missing in the get metric statistics request";
            LOGGER.error(errMsg);
            throw new DalException(errMsg);
        }

        retVal = QueryBuilders.terms().field(NAMESPACE_FIELD_PATH).terms(termsBuilder -> termsBuilder.value(
                Collections.singletonList(FieldValue.of(reqNamespace)))).build();

        return retVal;
    }
    // endregion

    // region Aggregations
    public void setAggregations(SearchRequest.Builder srb) throws DalException, IOException {
        List<String> dimensionKeys = request.getDimensionKeys();

        if (CollectionUtils.isNotEmpty(dimensionKeys)) {
            String dimensionPathFormat = "%s.%s";
            for (String dimensionKey : dimensionKeys) {
                if (StringUtils.isNotEmpty(dimensionKey)) {
                    String fieldPath = String.format(dimensionPathFormat, DIMENSIONS_PATH_PREFIX, dimensionKey);
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
        HitsMetadata<ElasticDimensionsValuesResponse> outerHits     = searchResponse.hits();

        TotalHits totalHits = outerHits.total();
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
                        Object dimensionValue = t.key();

                        if (dimensionValue != null) {
                            dimValuesResultSet.add(dimensionValue);
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
