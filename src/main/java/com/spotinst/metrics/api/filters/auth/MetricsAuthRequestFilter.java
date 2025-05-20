package com.spotinst.metrics.api.filters.auth;

import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.dropwizard.common.filters.auth.BaseAuthenticationRequestFilter;
import com.spotinst.dropwizard.common.filters.auth.FilterOptions;
import com.spotinst.metrics.MetricsAppContext;
import jakarta.annotation.Priority;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_INBOUND_RESOURCE_AUTH_FILTER;

@Priority(PRIORITY_INBOUND_RESOURCE_AUTH_FILTER)
public class MetricsAuthRequestFilter extends BaseAuthenticationRequestFilter {

    public MetricsAuthRequestFilter() {
    }

    @Override
    protected FilterOptions getFilterOptions() {
        return FilterOptions.ISSUER_ONLY;
    }

    @Override
    protected BaseAppContext getServiceContext() {
        return MetricsAppContext.getInstance();
    }
}
