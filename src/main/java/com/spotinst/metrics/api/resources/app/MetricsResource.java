package com.spotinst.metrics.api.resources.app;

import com.spotinst.commons.response.api.ServiceEmptyResponse;
import com.spotinst.commons.response.api.items.ServiceResponseItems;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.metrics.api.requests.ApiMetricStatisticsRequest;
import com.spotinst.metrics.api.requests.ApiMetricsCreateRequest;
import com.spotinst.metrics.api.responses.ApiMetricStatisticsResponse;
import com.spotinst.metrics.bl.cmds.CreateMetricCmd;
import com.spotinst.metrics.bl.cmds.GetMetricsStatisticsCmd;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.commons.validators.OverriddenIndexValidation;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

import java.io.IOException;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.MDC.MDC_NAME_ACTION;
import static com.spotinst.metrics.commons.constants.MetricsConstants.Resources.METRICS_RESOURCE_PATH;

/**
 * Created by Tal.Geva on 07/08/2024
 */
@Path(METRICS_RESOURCE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsResource.class);
    private static final String METRICS_RESOURCE_KIND   = "spotinst:metrics";
    private static final String KIND_GET_STATISTICS       = METRICS_RESOURCE_KIND + ":statistics";


    @Context
    private ResourceContext resourceContext;

    @POST
    @Path("")
    //TODO Tal: Check ApiOperation and ManagedAsync usage
//    @ApiOperation("Create metrics")
    @ManagedAsync
    //TODO Tal: Ask Ziv if to return ServiceEmptyResponse or void
    public ServiceEmptyResponse createMetrics(@Suspended AsyncResponse asyncResponse,@Valid ApiMetricsCreateRequest request, @OverriddenIndexValidation @QueryParam("index") String index
                                             ){
        RequestsContextManager.loadContext(resourceContext);
        MDC.put(MDC_NAME_ACTION, "createMetrics");
        LOGGER.debug("Start reporting metric data...");

        BlMetricCreateRequest blCreateRequest = MetricReportConverter.apiToBl(request);

        CreateMetricCmd cmd = new CreateMetricCmd(blCreateRequest);
        cmd.execute();

        ServiceEmptyResponse retVal = new ServiceEmptyResponse();

        LOGGER.debug("Finished reporting metric data");
        return retVal;

    }

    @POST
    @Path("/query")
    public ServiceResponseItems getMetricsStatistics(@Valid ApiMetricStatisticsRequest request, @OverriddenIndexValidation @QueryParam("index") String index) throws IOException {
        LOGGER.debug("Start fetching metric statistics...");

        BlMetricStatisticsRequest req = MetricStatisticConverter.toBl(request);

        GetMetricsStatisticsCmd    cmd      = new GetMetricsStatisticsCmd();
        BlMetricStatisticsResponse response = cmd.execute(req, index);

        ServiceResponseItems retVal = new ServiceResponseItems(KIND_GET_STATISTICS);
        if(response != null && response.isEmpty() == false) {
            ApiMetricStatisticsResponse resp = MetricReportConverter.blToApi(response);
            retVal.add(resp);
        }

        LOGGER.debug("Finished fetching metric statistics");
        return retVal;
    }
}
