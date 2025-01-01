package com.spotinst.metrics.api.filters.auth;

import com.spotinst.metrics.api.filters.account.MetricsResolveAccountIdRequestFilter;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.PRIORITY_AUTHENTICATION_FEATURE;
import static com.spotinst.dropwizard.common.constants.ServiceConstants.Resources.RESOURCE_NAME_HELLO_WORLD;

@Provider
@Priority(PRIORITY_AUTHENTICATION_FEATURE)
public class InboundAuthFeature implements DynamicFeature {
    //region Members
    private static final String SWAGGER_CLASS = "swagger";
    //endregion

    //region Override Methods
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        // Skip hello world filter & admin resource
        if (resourceInfo.getResourceMethod().getDeclaringClass().getName().contains(SWAGGER_CLASS) == false) {
            String simpleName = resourceInfo.getResourceMethod().getDeclaringClass().getSimpleName();

            switch (simpleName) {
                case RESOURCE_NAME_HELLO_WORLD: {
                    // Do nothing
                    break;
                }
                default: {
                    context.register(MetricsAuthRequestFilter.class);
                    context.register(MetricsResolveAccountIdRequestFilter.class);
                }
            }
        }
    }
    //endregion

}
