package com.spotinst.metrics.api.filters.account;

import com.spotinst.dropwizard.common.filters.ResolveAccountIdRequestFilter;
import com.spotinst.dropwizard.common.filters.auth.AccountQueryParamEnum;
import jakarta.annotation.Priority;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_INBOUND_RESOLVE_ACCOUNT_ID_FILTER;

/**
 * Created by zachi.nachshon on 8/1/17.
 */
@Priority(PRIORITY_INBOUND_RESOLVE_ACCOUNT_ID_FILTER)
public class MetricsResolveAccountIdRequestFilter extends ResolveAccountIdRequestFilter {

    @Override
    protected AccountQueryParamEnum getAccountIdQueryParam() {
        return AccountQueryParamEnum.SPOTINST_ACCOUNT_ID;
    }
}
