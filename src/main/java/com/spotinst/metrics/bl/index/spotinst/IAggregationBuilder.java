package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import co.elastic.clients.elasticsearch.core.SearchRequest;

import java.io.IOException;

public interface IAggregationBuilder {
    void setAggregations(SearchRequest.Builder searchRequestBuilder) throws DalException, IOException;

}
