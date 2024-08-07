package com.spotinst.metrics;

import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.metrics.bl.model.BlOrganization;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import org.elasticsearch.client.RestHighLevelClient;

import java.math.BigInteger;
import java.util.Map;

public class MetricsAppContext extends BaseAppContext<MetricsConfiguration, BlOrganization> {

    private static MetricsAppContext instance;

    private MetricsAppContext() {
    }

    public static MetricsAppContext getInstance() {

        if (instance == null) {
            synchronized (MetricsAppContext.class) {
                if (instance == null) {
                    instance = new MetricsAppContext();
                }
            }
        }
        return instance;
    }

    //region Members
    private Map<BigInteger, BlOrganization> organizations;
    private RestHighLevelClient             elasticClient;

    //endregion

    //region Getters and Setters

    @Override
    public Map<BigInteger, BlOrganization> getOrganizations() {
        return organizations;
    }

    @Override
    public void setOrganizations(Map<BigInteger, BlOrganization> organizations) {
        this.organizations = organizations;
    }

    public RestHighLevelClient getElasticClient() {
        return elasticClient;
    }

    public void setElasticClient(RestHighLevelClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    //endregion
}
