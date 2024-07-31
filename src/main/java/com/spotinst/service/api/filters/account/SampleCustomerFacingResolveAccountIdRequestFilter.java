package com.spotinst.service.api.filters.account;

import com.spotinst.dropwizard.bl.SpotinstAdministration;
import com.spotinst.dropwizard.common.filters.ResolveAccountIdRequestFilter;
import com.spotinst.dropwizard.common.filters.auth.AccountQueryParamEnum;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.bl.repos.SampleRepoManager;

import jakarta.annotation.Priority;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_INBOUND_RESOLVE_ACCOUNT_ID_FILTER;

/**
 * Created by zachi.nachshon on 8/1/17.
 */
@Priority(PRIORITY_INBOUND_RESOLVE_ACCOUNT_ID_FILTER)
public class SampleCustomerFacingResolveAccountIdRequestFilter extends ResolveAccountIdRequestFilter {

    private SpotinstAdministration spotinstAdministration;

    public SampleCustomerFacingResolveAccountIdRequestFilter() {
        this.spotinstAdministration =
                new SpotinstAdministration<>(() -> SampleAppContext.getInstance().getOrganizations(),
                                           id -> SampleRepoManager.Organization.get(id));
    }

    @Override
    protected boolean failOnUnresolvedAccount() {
        return true;
    }

    @Override
    protected boolean failOnInvalidAccountPattern() {
        return true;
    }

    @Override
    protected AccountQueryParamEnum getAccountIdQueryParam() {
        return AccountQueryParamEnum.ACCOUNT_ID;
    }

    @Override
    protected SpotinstAdministration fallbackToDefaultAccount() {
        return spotinstAdministration;
    }
}
