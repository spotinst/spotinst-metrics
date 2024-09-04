//package com.spotinst.metrics.bl.index.custom;
//
//import com.spotinst.metrics.MetricsAppContext;
//import com.spotinst.metrics.commons.configuration.ElasticSearchConfig;
//import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
//import com.spotinst.metrics.commons.configuration.QueryConfig;
//import com.spotinst.metrics.commons.constants.MetricsConstants;
//import com.spotinst.dropwizard.common.exceptions.dal.DalException;
//import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
//import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.index.query.*;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.aggregations.Aggregation;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.*;
//
//import static com.spotinst.metrics.commons.constants.MetricsConstants.Dimension.DIMENSIONS_PATH_PREFIX;
//import static com.spotinst.metrics.commons.constants.MetricsConstants.ElasticMetricConstants.OPTIMIZED_INDEX_PREFIX;
//import static com.spotinst.metrics.commons.constants.MetricsConstants.FieldPath.METRIC_KEYWORD_SUFFIX;
//
///**
// * Created by zachi.nachshon on 4/24/17.
// */
//public class DimensionsIndexManager {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionsIndexManager.class);
//
//    // If service configured to read from optimized index, field paths should exclude the use of 'keyword'
//    private static Boolean IS_READ_FROM_OPTIMIZED_INDEX;
//    private static Integer DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT;
//
//    private ElasticDimensionsValuesRequest request;
//
//    static {
//        ElasticSearchConfig config = MetricsAppContext.getInstance()
//                                                        .getConfiguration()
//                                                        .getElasticsearch();
//
//        // By default, service shouldn't read from optimized index (may be changed later on)
//        IS_READ_FROM_OPTIMIZED_INDEX = false;
//
//        IndexNamePatterns indexNamePatterns = config.getIndexNamePatterns();
//        if(indexNamePatterns != null) {
//            String readPattern = indexNamePatterns.getReadPattern();
//            if(readPattern != null) {
//                String idxPrefix = StringUtils.isEmpty(readPattern) ? "" : readPattern;
//                IS_READ_FROM_OPTIMIZED_INDEX = idxPrefix.startsWith(OPTIMIZED_INDEX_PREFIX);
//            }
//        }
//
//        QueryConfig queryConfig = MetricsAppContext.getInstance().getConfiguration().getQueryConfig();
//        DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT = queryConfig.getDimensionsValuesResultPerBucketLimit();
//    }
//
//    public DimensionsIndexManager(ElasticDimensionsValuesRequest request) {
//        this.request = request;
//    }
//
//    // region Filters
//    public void setFilters(SearchRequestBuilder srb) throws DalException {
//        TermsQueryBuilder        namespaceQuery     = createNamespaceQuery();
//        BoolQueryBuilder         dataOwnershipQuery = createDataOwnershipQuery();
//        List<ExistsQueryBuilder> existsQueries      = createDimensionsExistsQueries();
//
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//
//        boolQuery.minimumShouldMatch("1");
//
//        // Use filter instead of query to benefit from elastic search cache and avoid document scoring
//        boolQuery.filter(namespaceQuery);
//
//        // Add exists queries on requests dimensions keys with OR operator between them
//        existsQueries.forEach(boolQuery::should);
//
//        // Append account id on filter to enforce data ownership
//        boolQuery = boolQuery.filter(dataOwnershipQuery);
//
//        srb.setQuery(boolQuery);
//
//        // Queries should use the request cache if possible
//        srb.setRequestCache(true);
//
//        srb.setSize(0);
//    }
//
//    private List<ExistsQueryBuilder> createDimensionsExistsQueries() throws DalException {
//        List<ExistsQueryBuilder> existQueries  = new ArrayList<>();
//        List<String>             dimensionKeys = request.getDimensionKeys();
//
//        if(dimensionKeys == null || dimensionKeys.isEmpty()) {
//            String errMsg = "dimensions keys are missing in the get metric dimensions request";
//            LOGGER.error(errMsg);
//            throw new DalException(errMsg);
//        }
//
//        String format = "%s.%s";
//        dimensionKeys.forEach(dimKey -> {
//            String dimName = String.format(format, DIMENSIONS_PATH_PREFIX, dimKey);
//            ExistsQueryBuilder existsQ = QueryBuilders.existsQuery(dimName);
//            existQueries.add(existsQ);
//        });
//
//        return existQueries;
//    }
//
//    protected TermsQueryBuilder createNamespaceQuery() throws DalException {
//        TermsQueryBuilder retVal;
//        Set<String>       namespaces   = new HashSet<>();
//        String            reqNamespace = request.getNamespace();
//
//        if(StringUtils.isEmpty(reqNamespace)) {
//            String errMsg = "[namespace] is missing in the get metric statistics request";
//            LOGGER.error(errMsg);
//            throw new DalException(errMsg);
//        } else {
//            namespaces.add(reqNamespace);
//        }
//
//        retVal = new TermsQueryBuilder(MetricsConstants.ElasticMetricConstants.NAMESPACE_FIELD_PATH, namespaces);
//        return retVal;
//    }
//
//    protected BoolQueryBuilder createDataOwnershipQuery() {
//        String           accountId     = request.getAccountId();
//        BoolQueryBuilder retVal        = QueryBuilders.boolQuery();
//        String           accountIdPath = MetricsConstants.Dimension.ACCOUNT_ID_RAW_DIMENSION_NAME;
//
//        // If reading from an optimized index created by template, there is no need to append the '.keyword' suffix
//        if(IS_READ_FROM_OPTIMIZED_INDEX == false) {
//            accountIdPath += METRIC_KEYWORD_SUFFIX;
//        }
//
//        TermQueryBuilder accountIdQuery = QueryBuilders.termQuery(accountIdPath, accountId);
//
//        retVal.should(accountIdQuery);
//
//        return retVal;
//    }
//    // endregion
//
//    // region Aggregations
//    public void setAggregations(SearchRequestBuilder srb)
//            throws DalException, IOException {
//
//        List<String> dimensionKeys = request.getDimensionKeys();
//
//        if(dimensionKeys != null && dimensionKeys.isEmpty() == false) {
//            String dimensionPathFormat = "%s.%s";
//            for (String dimensionName : dimensionKeys) {
//                if (StringUtils.isEmpty(dimensionName) == false) {
//                    TermsAggregationBuilder dimAgg = AggregationBuilders.terms(dimensionName);
//                    dimAgg.minDocCount(1);
//                    dimAgg.shardMinDocCount(1);
//                    dimAgg.size(DIMENSIONS_VALUES_RESULT_PER_BUCKET_LIMIT);
//
//                    String fieldPath = String.format(dimensionPathFormat, DIMENSIONS_PATH_PREFIX, dimensionName);
//                    dimAgg.field(fieldPath);
//
//                    srb.addAggregation(dimAgg);
//                }
//            }
//        }
//    }
//    // endregion
//
//    // region Parse Response
//    public ElasticDimensionsValuesResponse parseResponse(SearchResponse searchResponse) throws DalException {
//        List<String>               dimensionKeys = request.getDimensionKeys();
//        SearchHits                 searchHits    = searchResponse.getHits();
//        ElasticDimensionsValuesResponse retVal        = null;
//
//        if(searchHits.getTotalHits().value > 0) {
//            Map<String, Set<Object>> resultMap      = new HashMap<>();
//            Aggregations             allBucketsAggs = searchResponse.getAggregations();
//
//            // Create a Map structure with dimensions as keys and empty linked list as values
//            dimensionKeys.forEach(d -> resultMap.put(d, new HashSet<>()));
//
//            for (String dimensionKey : dimensionKeys) {
//                Aggregation bucketAgg = allBucketsAggs.get(dimensionKey);
//                if (bucketAgg != null) {
//                    Set<Object>        dimValuesResultSet = resultMap.get(dimensionKey);
//                    StringTerms              bucketsTerms = (StringTerms) bucketAgg;
//                    List<StringTerms.Bucket> terms        = bucketsTerms.getBuckets();
//
//                    if(terms != null && terms.isEmpty() == false) {
//                        terms.forEach(t -> {
//                            Object dimensionValue = t.getKey();
//                            if(dimensionValue != null ) {
//                                dimValuesResultSet.add(dimensionValue);
//                            }
//                        });
//                    }
//                }
//            }
//
//            retVal = new ElasticDimensionsValuesResponse();
//            retVal.setDimensions(resultMap);
//        }
//
//        return retVal;
//    }
//    // endregion
//}
