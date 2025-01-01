package com.spotinst.metrics.bl.cmds.metric;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.errors.ErrorCodes;
import com.spotinst.metrics.bl.model.request.BlPercentilesRequest;
import com.spotinst.metrics.bl.model.response.BlPercentilesResponse;
import com.spotinst.metrics.bl.repos.RepoManager;
import com.spotinst.metrics.commons.utils.ContextUtils;
import com.spotinst.metrics.dal.services.elastic.IElasticSearchPercentilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetPercentilesCmd {

    //region Members
    private static final Logger                           LOGGER = LoggerFactory.getLogger(GetPercentilesCmd.class);
    private              IElasticSearchPercentilesService elasticSearchPercentilesService;
    //endregion

    //region Constructors
    public GetPercentilesCmd() {
        this.elasticSearchPercentilesService = MetricsAppContext.getInstance().getElasticSearchPercentilesService();
    }
    //endregion

    //region Public Methods
    public BlPercentilesResponse execute(BlPercentilesRequest request, String index) {
        ContextUtils.setCtxAccountOnRequest(request);
        BlPercentilesResponse retVal;

        RepoGenericResponse<BlPercentilesResponse> response =
                RepoManager.percentilesRepo.getPercentiles(request, index);

        if (response.isRequestSucceed() == false || response.getValue() == null) {
            LOGGER.error(String.format("Failed to get percentiles: %s", request.toString()), response.getDalErrors());
            throw new BlException(ErrorCodes.FAILED_TO_GET_PERCENTILES, "Failed to get percentiles");
        }

        LOGGER.info("Finished fetching percentiles");

        retVal = response.getValue();
        return retVal;
    }
    //endregion
}