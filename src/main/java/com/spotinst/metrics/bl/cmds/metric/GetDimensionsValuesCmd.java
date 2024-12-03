package com.spotinst.metrics.bl.cmds.metric;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.errors.ErrorCodes;
import com.spotinst.metrics.bl.model.BlDimensionsValuesRequest;
import com.spotinst.metrics.bl.model.responses.BlDimensionsValuesResponse;
import com.spotinst.metrics.bl.repos.MetricsRepoManager;
import com.spotinst.metrics.commons.configuration.QueryConfig;
import com.spotinst.metrics.commons.utils.ContextUtils;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Namespace.BALANCER_NAMESPACE_NEW;

public class GetDimensionsValuesCmd extends BaseCmd<BlDimensionsValuesResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDimensionsValuesCmd.class);

    private static Integer DIMENSIONS_KEYS_REQUEST_LIMIT;

    static {
        QueryConfig queryConfig = MetricsAppContext.getInstance().getConfiguration().getQueryConfig();
        DIMENSIONS_KEYS_REQUEST_LIMIT = queryConfig.getDimensionsKeysRequestLimit();
    }

    public GetDimensionsValuesCmd() {
        super(null, null);
    }

    public BlDimensionsValuesResponse execute(BlDimensionsValuesRequest request, String index) {

        LOGGER.info("Start fetching dimensions values...");

        // Validate namespace does not contain 'spotinst/balancer' prefix
        validateNamespace(request);

        // Validate the amount of unique dimensions does not exceed the configurable value
        validateDimensionKeysAmount(request);

        // Set account id on metric stats request to enforce data ownership
        ContextUtils.setCtxAccountOnRequest(request);

        // Data ownership is being enforced by placing the account id from the context on the elastic search query
        BlDimensionsValuesResponse                      retVal;
        RepoGenericResponse<BlDimensionsValuesResponse> response = MetricsRepoManager.metricRepo.getDimensionsValues(request, index);

        if (response.isRequestSucceed() == false || response.getValue() == null) {
            LOGGER.error(String.format("Failed to get dimensions values: %s", request.toString()),
                         response.getDalErrors());
            throw new BlException(ErrorCodes.FAILED_TO_GET_DIMENSIONS_VALUES, "Failed to get dimensions values");
        }

        LOGGER.info("Finished fetching dimensions values");

        retVal = response.getValue();
        return retVal;
    }

    private void validateDimensionKeysAmount(BlDimensionsValuesRequest request) {
        Set<String> dimKeysDistinct = request.getDimensionKeys().stream().distinct().collect(Collectors.toSet());
        int         dimKeysAmount   = dimKeysDistinct.size();

        if (dimKeysAmount > DIMENSIONS_KEYS_REQUEST_LIMIT) {
            String errFormat = "Reached the limit of allowed dimension values per request, limit is [%s]";
            String errMsg    = String.format(errFormat, DIMENSIONS_KEYS_REQUEST_LIMIT);
            LOGGER.error(errMsg);
            throw new BlException(ErrorCodes.FAILED_TO_GET_DIMENSIONS_VALUES, errMsg);
        }
    }

    private void validateNamespace(BlDimensionsValuesRequest request) {
        String namespaceLC = request.getNamespace().toLowerCase();

        if(namespaceLC.startsWith(BALANCER_NAMESPACE_NEW)) {
            String format = "Namespaces starting with [%s] are reserved";
            String errMsg = String.format(format, BALANCER_NAMESPACE_NEW);
            LOGGER.error(errMsg);
            throw new BlException(ErrorCodes.FAILED_TO_GET_DIMENSIONS_VALUES, errMsg);
        }
    }
}