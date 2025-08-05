package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.model.request.BlPercentilesRequest;
import com.spotinst.metrics.bl.model.response.BlPercentilesResponse;
import com.spotinst.metrics.bl.repos.interfaces.IPercentilesRepo;
import com.spotinst.metrics.commons.converters.PercentilesConverter;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticPercentilesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticPercentilesResponse;
import com.spotinst.metrics.dal.services.elastic.IElasticSearchPercentilesService;
import com.spotinst.metrics.dal.services.elastic.metric.ElasticSearchPercentilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PercentilesRepo implements IPercentilesRepo {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(PercentilesRepo.class);

    private ElasticSearchPercentilesService elasticPercentilesService;

    public PercentilesRepo() {
        initInjections();
    }

    private void initInjections() {
        this.elasticPercentilesService = new ElasticSearchPercentilesService();
    }

    //region Override Methods


    @Override
    public RepoGenericResponse<BlPercentilesResponse> getPercentiles(BlPercentilesRequest request,
                                                                     String index) {
        RepoGenericResponse<BlPercentilesResponse> retVal;

        try {
            ElasticPercentilesRequest elasticPercentilesRequest = PercentilesConverter.toEs(request);
            ElasticPercentilesResponse elasticPercentilesResp =
                    elasticPercentilesService.getPercentiles(elasticPercentilesRequest, index);

            BlPercentilesResponse blPercentilesResp = PercentilesConverter.toBl(elasticPercentilesResp);
            retVal = new RepoGenericResponse<>(blPercentilesResp);
        }
        catch (DalException ex) {
            retVal = ExceptionHelper.handleDalException(ex);
        }

        return retVal;
    }
}
