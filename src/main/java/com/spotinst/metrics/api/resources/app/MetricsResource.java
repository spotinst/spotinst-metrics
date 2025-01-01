package com.spotinst.metrics.api.resources.app;

import com.spotinst.commons.response.api.ServiceEmptyResponse;
import com.spotinst.commons.response.api.items.ServiceResponseItems;
import com.spotinst.metrics.api.model.request.ApiDimensionsValuesRequest;
import com.spotinst.metrics.api.model.request.ApiMetricStatisticsRequest;
import com.spotinst.metrics.api.model.request.ApiMetricsReportRequest;
import com.spotinst.metrics.api.model.request.ApiPercentilesRequest;
import com.spotinst.metrics.api.model.response.ApiDimensionsValuesResponse;
import com.spotinst.metrics.api.model.response.ApiMetricStatisticsResponse;
import com.spotinst.metrics.api.model.response.ApiPercentilesResponse;
import com.spotinst.metrics.bl.cmds.dimension.GetDimensionsValuesCmd;
import com.spotinst.metrics.bl.cmds.metric.GetMetricsStatisticsCmd;
import com.spotinst.metrics.bl.cmds.metric.GetPercentilesCmd;
import com.spotinst.metrics.bl.cmds.metric.ReportMetricsCmdRunnable;
import com.spotinst.metrics.bl.model.request.BlDimensionsValuesRequest;
import com.spotinst.metrics.bl.model.request.BlMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.request.BlPercentilesRequest;
import com.spotinst.metrics.bl.model.response.BlDimensionsValuesResponse;
import com.spotinst.metrics.bl.model.response.BlMetricStatisticsResponse;
import com.spotinst.metrics.bl.model.response.BlPercentilesResponse;
import com.spotinst.metrics.commons.converters.DimensionsValuesConverter;
import com.spotinst.metrics.commons.converters.MetricReportConverter;
import com.spotinst.metrics.commons.converters.MetricStatisticConverter;
import com.spotinst.metrics.commons.converters.PercentilesConverter;
import com.spotinst.metrics.commons.threadpool.ReportMetricsCmdExecutor;
import com.spotinst.metrics.commons.validators.OverriddenIndexValidation;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.io.IOException;

import static com.spotinst.metrics.commons.constants.MetricsConstants.Resources.METRICS_RESOURCE_PATH;

/**
 * Created by Tal.Geva on 07/08/2024
 */
@Path(METRICS_RESOURCE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    private static final Logger LOGGER                    = LoggerFactory.getLogger(MetricsResource.class);
    private static final String METRICS_RESOURCE_KIND     = "spotinst:metrics";
    private static final String KIND_GET_STATISTICS       = METRICS_RESOURCE_KIND + ":statistics";
    private static final String KIND_GET_DIMENSION_VALUES = METRICS_RESOURCE_KIND + ":dimension:values";
    private static final String KIND_GET_PERCENTILES      = KIND_GET_STATISTICS + ":percentiles";

    @POST
    @Path("")
    public ServiceEmptyResponse reportMetrics(@Suspended AsyncResponse asyncResponse,
                                              @Valid ApiMetricsReportRequest request,
                                              @OverriddenIndexValidation @QueryParam("index") String index) {
        LOGGER.debug("Start reporting metric data...");

        ReportMetricsCmdRunnable worker = new ReportMetricsCmdRunnable(request, index);
        ReportMetricsCmdExecutor.getInstance().execute(worker);

        ServiceEmptyResponse retVal = new ServiceEmptyResponse();
        asyncResponse.resume(retVal);

        LOGGER.debug("Finished reporting metric data");
        return retVal;
    }

    @POST
    @Path("/query")
    public ServiceResponseItems getMetricsStatistics(@Valid ApiMetricStatisticsRequest request,
                                                     @OverriddenIndexValidation @QueryParam("index") String index) {
        LOGGER.debug("Start fetching metric statistics...");

        BlMetricStatisticsRequest req = MetricStatisticConverter.toBl(request);

        GetMetricsStatisticsCmd    cmd      = new GetMetricsStatisticsCmd();
        BlMetricStatisticsResponse response = cmd.execute(req, index);

        ServiceResponseItems retVal = new ServiceResponseItems(KIND_GET_STATISTICS);

        if (response != null && response.isEmpty() == false) {
            ApiMetricStatisticsResponse resp = MetricReportConverter.blToApi(response);
            retVal.add(resp);
            LOGGER.debug("Finished fetching metric statistics");
        }
        else {
            LOGGER.warn("Failed to get metric statistics");
        }

        return retVal;
    }

    @POST
    @Path("/query/dimensions")
    public ServiceResponseItems getDimensionsValues(@Valid ApiDimensionsValuesRequest request,
                                                    @OverriddenIndexValidation @QueryParam("index") String index) throws IOException {
        LOGGER.info("Start fetching dimensions values...");

        BlDimensionsValuesRequest req = DimensionsValuesConverter.toBl(request);

        GetDimensionsValuesCmd     cmd      = new GetDimensionsValuesCmd();
        BlDimensionsValuesResponse response = cmd.execute(req, index);

        ServiceResponseItems retVal = new ServiceResponseItems(KIND_GET_DIMENSION_VALUES);

        if (response != null && response.isEmpty() == false) {
            ApiDimensionsValuesResponse resp = DimensionsValuesConverter.toApi(response);
            retVal.add(resp);
        }

        LOGGER.info("Finished fetching dimensions values");
        return retVal;
    }

    @POST
    @Path("/query/percentiles")
    public ServiceResponseItems<ApiPercentilesResponse> getPercentiles(@Valid ApiPercentilesRequest request,
                                                                       @OverriddenIndexValidation @QueryParam("index") String index) throws IOException {
        LOGGER.debug("Start fetching percentiles statistics...");

        // convert to BL
        BlPercentilesRequest blRequest = PercentilesConverter.toBl(request);

        // execute the command
        GetPercentilesCmd     cmd      = new GetPercentilesCmd();
        BlPercentilesResponse blResult = cmd.execute(blRequest, index);

        // convert to API
        ApiPercentilesResponse                       apiResult = PercentilesConverter.toApi(blResult);
        ServiceResponseItems<ApiPercentilesResponse> retVal    = new ServiceResponseItems<>(KIND_GET_PERCENTILES);
        retVal.setItem(apiResult);

        return retVal;
    }
}
