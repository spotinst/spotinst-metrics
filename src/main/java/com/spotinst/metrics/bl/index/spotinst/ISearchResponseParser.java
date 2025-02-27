package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;


public interface ISearchResponseParser {
    ElasticMetricStatisticsResponse parseResponse(SearchResponse<ElasticMetricStatisticsResponse>  searchResponse) throws DalException;

}
