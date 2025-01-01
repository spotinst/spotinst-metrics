package com.spotinst.metrics.dal.services.elastic;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticPercentilesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticPercentilesResponse;

public interface IElasticSearchPercentilesService {
    ElasticPercentilesResponse getPercentiles(ElasticPercentilesRequest request, String index) throws DalException;
}
