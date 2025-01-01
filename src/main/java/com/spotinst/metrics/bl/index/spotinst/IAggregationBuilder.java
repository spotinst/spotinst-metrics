package com.spotinst.metrics.bl.index.spotinst;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public interface IAggregationBuilder {
    void setAggregations(SearchSourceBuilder sourceBuilder) throws DalException, IOException;

}
