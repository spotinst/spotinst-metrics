package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface IFilterBuilder {
    void setFilters(SearchSourceBuilder sourceBuilder) throws DalException;

}
