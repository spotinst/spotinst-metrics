package com.spotinst.service.api.resources.app;

import com.spotinst.commons.response.api.items.ServiceResponseItems;
import com.spotinst.commons.response.dal.DeleteInnerResponse;
import com.spotinst.commons.response.dal.ServiceDeleteResponse;
import com.spotinst.commons.response.dal.ServiceUpdateResponse;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.service.api.models.dummy.*;
import com.spotinst.service.bl.cmds.dummy.*;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.commons.converters.Converters;
import com.spotinst.service.commons.enums.DummyEnum;
import com.spotinst.service.dal.models.db.dummy.DummyFilter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.spotinst.service.commons.constants.ServiceConstants.Resources.SAMPLE_RESOURCE_PATH;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
@Path(SAMPLE_RESOURCE_PATH)
@Api(SAMPLE_RESOURCE_PATH)
@ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
                required = true, dataType = "string", paramType = "header") })
@Produces(MediaType.APPLICATION_JSON)
public class SampleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleResource.class);

    @Context
    private ResourceContext resourceContext;

    @GET
    @Path("/dummy/{dummyId}")
    @ApiOperation("This is dummy path short description")
    public void getDummy(@Suspended AsyncResponse asyncResponse, @PathParam("dummyId") String dummyId) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info(String.format("Start fetching dummy by id [%s]...", dummyId));

        GetDummyCmd cmd = new GetDummyCmd();
        cmd.execute(dummyId);

        ServiceResponseItems<ApiDummyResponse> retVal = new ServiceResponseItems<>("sample:get_dummy");

        BlDummy result = cmd.getResult();
        if (result != null) {
            ApiDummyResponse response = Converters.Dummy.toApi(result);
            retVal.setItem(response);
        }

        LOGGER.info(String.format("Successfully fetched dummy by id [%s]", dummyId));
        asyncResponse.resume(retVal);
    }

    @GET
    @Path("/dummy")
    @ApiOperation("This is dummy path short description")
    public void getAllDummies(@Suspended AsyncResponse asyncResponse, @QueryParam("name") String name,
                              @QueryParam("type") String type, @QueryParam("accountId") String accountId) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info("Start fetching all dummies by filter...");

        DummyFilter filter = new DummyFilter(accountId, name, DummyEnum.fromName(type));

        GetAllDummiesCmd cmd = new GetAllDummiesCmd();
        cmd.execute(accountId, filter);

        ServiceResponseItems<ApiDummyResponse> retVal = new ServiceResponseItems<>("sample:get_all_dummies");

        List<ApiDummyResponse> dummyResponse;
        List<BlDummy>          dummies = cmd.getResult();
        if (dummies != null) {
            dummyResponse = new ArrayList<>();
            for (BlDummy blDummy : dummies) {
                ApiDummyResponse aResp = Converters.Dummy.toApi(blDummy);
                dummyResponse.add(aResp);
            }
            retVal.setItems(dummyResponse);
        }

        LOGGER.info("Finished getting all dummies by filter");
        asyncResponse.resume(retVal);
    }

    @POST
    @Path("/dummy")
    @ApiOperation("This is dummy path short description")
    public void createDummy(@Suspended AsyncResponse asyncResponse, ApiDummyCreateRequest request) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info("Start creating dummy...");

        ApiDummyCreateInner innerDummy = request.getDummy();
        BlDummy             blDummy    = Converters.Dummy.toBl(innerDummy);
        CreateDummyCmd      cmd        = new CreateDummyCmd();
        cmd.execute(blDummy);

        ServiceResponseItems<ApiDummyResponse> retVal = new ServiceResponseItems<>("sample:create_dummy");

        BlDummy createdDummy = cmd.getResult();

        if (createdDummy != null) {
            ApiDummyResponse response = Converters.Dummy.toApi(createdDummy);
            retVal.add(response);
        }

        LOGGER.info("Successfully created dummy");
        asyncResponse.resume(retVal);
    }

    @PUT
    @Path("/dummy/{dummyId}")
    @ApiOperation("This is dummy path short description")
    public void updateDummy(@Suspended AsyncResponse asyncResponse, @PathParam("dummyId") String dummyId,
                            ApiDummyUpdateRequest request) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info(String.format("Start updating dummy with id [%s]...", dummyId));

        ApiDummyUpdateInner innerDummy = request.getDummy();
        BlDummy             blDummy    = Converters.Dummy.toBl(innerDummy);
        UpdateDummyCmd      cmd        = new UpdateDummyCmd();
        cmd.execute(dummyId, blDummy);

        ServiceUpdateResponse retVal = new ServiceUpdateResponse();

        LOGGER.info(String.format("Successfully updated dummy with id [%s]", dummyId));
        asyncResponse.resume(retVal);
    }

    @DELETE
    @Path("/dummy/{dummyId}")
    @ApiOperation("This is dummy path short description")
    public void deleteDummy(@Suspended AsyncResponse asyncResponse,
                            @PathParam("dummyId") String dummyId) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info(String.format("Starting to delete dummy [%s]...", dummyId));

        DeleteDummyCmd cmd = new DeleteDummyCmd();
        cmd.execute(dummyId);

        DeleteInnerResponse response = new DeleteInnerResponse();
        response.setKind("sample:delete_dummy");

        Boolean result = cmd.getResult();
        response.setRemoved(result);

        ServiceDeleteResponse retVal = new ServiceDeleteResponse();

        LOGGER.info(String.format("Successfully deleted dummy [%s]", dummyId));
        asyncResponse.resume(retVal);
    }
}
