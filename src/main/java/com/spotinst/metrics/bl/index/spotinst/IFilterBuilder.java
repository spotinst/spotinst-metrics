package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import co.elastic.clients.elasticsearch.core.SearchRequest;

public interface IFilterBuilder {
    void setFilters(SearchRequest.Builder searchRequestBuilder) throws DalException;

}
