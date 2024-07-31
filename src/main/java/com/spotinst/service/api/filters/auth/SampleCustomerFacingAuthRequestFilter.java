package com.spotinst.service.api.filters.auth;

import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.dropwizard.common.filters.auth.BaseAuthenticationRequestFilter;
import com.spotinst.dropwizard.common.filters.auth.FilterOptions;
import com.spotinst.dropwizard.common.filters.auth.IAuthTokenActions;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.dal.services.ums.UmsService;

import jakarta.annotation.Priority;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_INBOUND_RESOURCE_AUTH_FILTER;

@Priority(PRIORITY_INBOUND_RESOURCE_AUTH_FILTER)
public class SampleCustomerFacingAuthRequestFilter extends BaseAuthenticationRequestFilter {

    private IAuthTokenActions authTokenActions;

    public SampleCustomerFacingAuthRequestFilter() {

        // TODO: This is a basic example for getting the feel on how to use the auth filter
        // TODO: Use proper caching instead of a direct call to the UMS service on token actions
        this.authTokenActions = new UmsService();
    }

    @Override
    protected IAuthTokenActions getAuthTokenActions() {
        return authTokenActions;
    }

    @Override
    protected FilterOptions getFilterOptions() {
        return FilterOptions.UMS_WITH_ISSUER;
    }

    @Override
    protected BaseAppContext getServiceContext() {
        return SampleAppContext.getInstance();
    }
}
