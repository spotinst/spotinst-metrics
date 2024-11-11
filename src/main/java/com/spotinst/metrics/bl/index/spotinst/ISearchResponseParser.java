package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import org.elasticsearch.action.search.SearchResponse;

public interface ISearchResponseParser {
    ElasticMetricStatisticsResponse parseResponse(SearchResponse searchResponse) throws DalException;

}
