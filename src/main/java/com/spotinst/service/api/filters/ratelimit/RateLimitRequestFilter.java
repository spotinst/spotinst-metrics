package com.spotinst.service.api.filters.ratelimit;

import com.spotinst.dropwizard.common.constants.ServiceConstants;
import com.spotinst.dropwizard.common.ratelimit.SpotinstRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Created by zachi.nachshon on 16/07/2018.
 */
@Provider
@Priority(ServiceConstants.Filters.PRIORITY_INBOUND_RATE_LIMIT_FILTER)
public class RateLimitRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitRequestFilter.class);

    public RateLimitRequestFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext) {
        SpotinstRateLimiter.getInstance().acquire(requestContext);
    }
}
