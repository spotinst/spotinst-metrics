package com.spotinst.service.api.filters.ratelimit;

import com.spotinst.dropwizard.common.constants.ServiceConstants;
import com.spotinst.dropwizard.common.ratelimit.SpotinstRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by zachi.nachshon on 16/07/2018.
 */
@Provider
@Priority(ServiceConstants.Filters.PRIORITY_OUTBOUND_RATE_LIMIT_FILTER)
public class RateLimitResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitResponseFilter.class);

    public RateLimitResponseFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        SpotinstRateLimiter.getInstance().release(requestContext, responseContext);
    }
}
