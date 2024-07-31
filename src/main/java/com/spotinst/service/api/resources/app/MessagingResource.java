package com.spotinst.service.api.resources.app;

import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.messaging.clients.producer.IMessagingProducer;
import com.spotinst.messaging.model.InternalMessageEnvelope;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.api.models.messaging.ApiInternalNotification;
import com.spotinst.service.commons.converters.Converters;
import com.spotinst.service.messaging.model.sample.SqsSampleMessageEntity;
import com.spotinst.service.messaging.model.notification.SqsInternalNotification;
import com.spotinst.service.messaging.topics.SampleMessageTopics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import static com.spotinst.service.commons.constants.ServiceConstants.Resources.MESSAGING_RESOURCE_PATH;
import static com.spotinst.service.commons.constants.ServiceConstants.Resources.SAMPLE_RESOURCE_PATH;
import static com.spotinst.service.messaging.producer.utils.MessageContextUtil.create;

/**
 * Note that SNS cannot publish messages to a FIFO queue
 * When working with SNS publisher, make sure to use only standard queues
 *
 * FIFO queues ensure:
 *  - Ordering
 *  - Prevent duplicate messages
 *
 * Created by zachi.nachshon on 4/12/18.
 */
@Path(MESSAGING_RESOURCE_PATH)
@Api(MESSAGING_RESOURCE_PATH)
@ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
                required = true, dataType = "string", paramType = "header") })
@Produces(MediaType.APPLICATION_JSON)
public class MessagingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingResource.class);

    @Context
    private ResourceContext resourceContext;

    @POST
    @Path("/sqs/publish/notification")
    @ApiOperation("This is messaging path short description")
    @ManagedAsync
    public void publishInternalNotification(
            @Suspended AsyncResponse asyncResponse,
            @Valid ApiInternalNotification internalNotification) {

        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info("Start sending internal notification to SQS FIFO/Standard queue...");

        SqsInternalNotification data = Converters.Notification.toSqs(internalNotification);

        InternalMessageEnvelope messageEnvelope
                = InternalMessageEnvelope.newBuilder()
                                         .setEntity(data)
                                         .setTopic(SampleMessageTopics.InternalNotification)
                                         .setContext(create())
                                         .build();

        IMessagingProducer sqsProducer = SampleAppContext.getInstance().getSqsProducer();
        sqsProducer.publishMessage(SampleMessageTopics.InternalNotification, messageEnvelope);

        LOGGER.info("Completed sending internal notification to SQS FIFO/Standard queue");
        asyncResponse.resume(JsonMapper.toJson(messageEnvelope));
    }

    @POST
    @Path("/sns/publish/notification")
    @ApiOperation("This is messaging path short description")
    @ManagedAsync
    public void publishSnsToSqsMessage(@Suspended AsyncResponse asyncResponse) {
        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info("Start producing SNS message to SQS Standard only queue...");

        SqsSampleMessageEntity data =
                new SqsSampleMessageEntity("Hey, I'm SNS to SQS Standard only");

        InternalMessageEnvelope messageEnvelope
                = InternalMessageEnvelope.newBuilder()
                                         .setEntity(data)
                                         .setTopic(SampleMessageTopics.InternalNotification)
                                         .setContext(create())
                                         .build();

        IMessagingProducer snsProducer = SampleAppContext.getInstance().getSnsProducer();
        snsProducer.publishMessage(SampleMessageTopics.InternalNotification, messageEnvelope);

        LOGGER.info("Completed attempt to produce SNS message to SQS Standard only queue");
        asyncResponse.resume(JsonMapper.toJson(messageEnvelope));
    }

    @POST
    @Path("/sqs/publish/sample")
    @ApiOperation("This is messaging path short description")
    @ManagedAsync
    public void publishSqsToSampleQueue(@Suspended AsyncResponse asyncResponse) {
        RequestsContextManager.loadContext(resourceContext);
        LOGGER.info("Start producing SQS message to SQS sample queue...");

        SqsSampleMessageEntity data =
                new SqsSampleMessageEntity("Hey, I'm an SQS sample entity");

        InternalMessageEnvelope messageEnvelope
                = InternalMessageEnvelope.newBuilder()
                                         .setEntity(data)
                                         .setTopic(SampleMessageTopics.SampleTopic)
                                         .setContext(create())
                                         .build();

        IMessagingProducer sqsProducer = SampleAppContext.getInstance().getSqsProducer();
        sqsProducer.publishMessage(SampleMessageTopics.SampleTopic, messageEnvelope);

        LOGGER.info("Completed attempt to produce SQS message to SQS sample queue");
        asyncResponse.resume(JsonMapper.toJson(messageEnvelope));
    }
}
