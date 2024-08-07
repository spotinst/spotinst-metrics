package com.spotinst.metrics.api.resources.app;

import com.spotinst.commons.response.api.ServiceResponseAffectedRows;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.metrics.api.requests.ApiMetricsCreateRequest;
import com.spotinst.metrics.bl.cmds.CreateMetricCmd;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.commons.converters.MetricConverter;
import io.swagger.annotations.ApiOperation;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.validation.Valid;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.MDC.MDC_NAME_ACTION;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Resources.METRICS_RESOURCE_PATH;

/**
 * Created by Tal.Geva on 07/08/2024
 */
@Path(METRICS_RESOURCE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsResource.class);

    @Context
    private ResourceContext resourceContext;

    @POST
    @Path("")
    //TODO Tal: Check ApiOperation and ManagedAsync usage
    @ApiOperation("Create metrics")
    @ManagedAsync
    //TODO Tal: Ask Ziv if to return ServiceEmptyResponse or void
    public void createMetrics(@Suspended AsyncResponse asyncResponse,@Valid ApiMetricsCreateRequest request
//            , @OverriddenIndexValidation @QueryParam("index") String index
                                             ){
        RequestsContextManager.loadContext(resourceContext);
        MDC.put(MDC_NAME_ACTION, "createMetrics");
        LOGGER.debug("Start reporting metric data...");

        BlMetricCreateRequest blCreateRequest = MetricConverter.apiToBl(request);

        CreateMetricCmd cmd = new CreateMetricCmd(blCreateRequest);
        cmd.execute();

        Integer createdDocumentCount = blCreateRequest.getMetricDocuments().size();

        ServiceResponseAffectedRows retVal = new ServiceResponseAffectedRows("metrics:createMetrics");
        retVal.setAffectedRows(createdDocumentCount);

        LOGGER.debug("Finished reporting metric data");
        asyncResponse.resume(retVal);

    }
}
