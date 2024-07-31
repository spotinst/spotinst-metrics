package com.spotinst.service.messaging.consumer.handler;

import com.spotinst.dropwizard.common.context.RequestContext;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.messaging.context.MessageContext;
import com.spotinst.messaging.context.MessageContextProvider;
import com.spotinst.messaging.model.PublisherInfo;

/**
 * Created by zachi.nachshon on 16/09/2018.
 */
public class BaseConsumerHandler {

    public BaseConsumerHandler() {}

    protected void loadRequestContext() {
        MessageContext msgContext = MessageContextProvider.getContext();

        if(msgContext != null) {
            RequestContext reqContext = RequestsContextManager.getContext();
            if(reqContext == null) {
                reqContext = new RequestContext(null);
            }

            reqContext.setOrganizationId(msgContext.getOrganizationId());
            reqContext.setAccountId(msgContext.getAccountId());
            reqContext.setUserId(msgContext.getUserId());
            reqContext.setLocalRequestId(msgContext.getLocalRequestId());

            PublisherInfo publisherInfo = msgContext.getPublisherInfo();
            String        requestId     = publisherInfo != null ? publisherInfo.getRequestId() : null;
            reqContext.setRequestId(requestId);

            RequestsContextManager.set(reqContext);
        }
    }
}
