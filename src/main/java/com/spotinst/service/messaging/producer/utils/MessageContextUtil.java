package com.spotinst.service.messaging.producer.utils;

import com.spotinst.dropwizard.common.context.RequestContext;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.messaging.context.MessageContext;
import com.spotinst.messaging.model.PublisherInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by zachi.nachshon on 23/08/2018.
 */
public class MessageContextUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageContextUtil.class);

    public static MessageContext create() {
        return MessageContextUtil.create(3);
    }

    public static MessageContext create(int numberOfRetries) {
        MessageContext retVal = null;
        RequestContext reqCtx = RequestsContextManager.getContext();

        if(reqCtx != null && reqCtx.getContainerRequestContext() != null) {
            ContainerRequestContext containerCtx = reqCtx.getContainerRequestContext();

            String  httpMethod = containerCtx.getMethod();
            UriInfo uriInfo    = containerCtx.getUriInfo();
            String  baseUri    = uriInfo.getBaseUri().toString();
            String  uriPath    = uriInfo.getPath();

            URI    uri    = uriInfo.getRequestUri();
            String client = uri.getHost();

            PublisherInfo publisherInfo = new PublisherInfo(reqCtx.getRequestId(), baseUri, uriPath, client, httpMethod);

            retVal = new MessageContext(reqCtx.getUserId(),
                                        reqCtx.getAccountId(),
                                        reqCtx.getOrganizationId(),
                                        -1L,
                                        publisherInfo,
                                        numberOfRetries);
        } else {
            LOGGER.error("Request context or container request context are missing, cannot create message context");
        }

        return retVal;
    }
}
