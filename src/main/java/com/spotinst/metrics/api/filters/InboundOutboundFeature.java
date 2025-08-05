package com.spotinst.metrics.api.filters;

import com.spotinst.dropwizard.common.filters.InboundRequestFilter;
import com.spotinst.dropwizard.common.filters.InboundRequestFilter.RequestFilterOptions;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_INBOUND_OUTBOUND_FEATURE;

@Priority(PRIORITY_INBOUND_OUTBOUND_FEATURE)
public class InboundOutboundFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        // Optional usage on filter classes when no configuration is needed
        //        context.register(InboundRequestFilter.class);
        //        context.register(OutboundResponseFilter.class);

        // Register request/response filters
        InboundRequestFilter reqFilter = createInboundRequestFilter();
        context.register(reqFilter);


        OutboundResponseFilter resFilter = createOutboundResponseFilter();
        context.register(resFilter);
    }

    private InboundRequestFilter createInboundRequestFilter() {
        RequestFilterOptions reqFilterOptions = new RequestFilterOptions();

        // Log request url with/without body
        // There is a performance penalty when logging the body of every request
        reqFilterOptions.enableLogRequestUrl(false);

        // Limit request size for customer facing service
        //        reqFilterOptions.limitRequestSize(256);

        InboundRequestFilter reqFilter = new InboundRequestFilter(reqFilterOptions);
        return reqFilter;
    }

    private OutboundResponseFilter createOutboundResponseFilter() {

        // Log response url, param, query, method, status-code, request time
        OutboundResponseFilter resFilter = new OutboundResponseFilter();

        // Exclude specific URL paths in case we'll need to maintain a custom JSON
        // as the service response and to avoid the Spotinst JSON response structure
        //        Set<String>            excludedPaths     = new HashSet<>(Collections.singletonList("/my/secret/path"));
        //        OutboundResponseFilter resFilterExcluded = new OutboundResponseFilter(excludedPaths, true);

        return resFilter;
    }
}
