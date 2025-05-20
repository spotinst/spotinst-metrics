package com.spotinst.metrics;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import com.spotinst.commons.exceptions.ApplicationRequirementsException;
import com.spotinst.commons.mapper.entities.registration.BaseMappingRegistration;
import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.dropwizard.common.filters.auth.token.TokenPayLoad;
import com.spotinst.dropwizard.common.ratelimit.GetAggregatedRateLimitSnapshotTask;
import com.spotinst.dropwizard.common.ratelimit.GetRateLimitSnapshotTask;
import com.spotinst.dropwizard.common.ratelimit.UpdateMySqlRateLimitsTask;
import com.spotinst.dropwizard.common.service.BaseApplication;
import com.spotinst.dropwizard.common.service.healthcheck.HelloWorldResource;
import com.spotinst.metrics.api.filters.InboundOutboundFeature;
import com.spotinst.metrics.api.filters.auth.InboundAuthFeature;
import com.spotinst.metrics.api.resources.app.MetricsResource;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import com.spotinst.metrics.dal.services.elastic.infra.ElasticSearchService;
import io.dropwizard.jobs.Job;
import io.dropwizard.servlets.tasks.Task;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MetricsApplication extends BaseApplication<MetricsConfiguration, TokenPayLoad> {

    private static final Logger  LOGGER                  = LoggerFactory.getLogger(MetricsApplication.class);
    private static final Boolean TRANSPORT_SNIFF_DEFAULT = false;

    // region Entry Point
    public static void main(String[] args) throws Exception {
        new MetricsApplication().run(args);
    }
    // endregion

    @Override
    public String getName() {
        return "spotinst-metrics";
    }

    @Override
    protected List<Object> getApiResources() {
        List<Object> apiResources = new ArrayList<>();
        // ALWAYS register resources with Class.class, or you will have ResourceContext == null
        apiResources.add(HelloWorldResource.class);
        apiResources.add(MetricsResource.class);
        return apiResources;
    }

    //TODO TAL OYAR - null was returned in the original code
    //TODO Tal: Maybe I should return null
    @Override
    protected List<Task> getAdminTasks() {
        List<Task> tasks = new ArrayList<>();

        // Rate Limit Tasks
        tasks.add(new GetRateLimitSnapshotTask());
        tasks.add(new GetAggregatedRateLimitSnapshotTask());
        tasks.add(new UpdateMySqlRateLimitsTask());

        return tasks;
    }

    @Override
    protected List<Class<?>> getFeaturesAndFilters() {
        List<Class<?>> features = new ArrayList<>();
        features.add(InboundAuthFeature.class);
        features.add(InboundOutboundFeature.class);
        return features;
    }

    @Override
    protected BaseAppContext getServiceContext() {
        return MetricsAppContext.getInstance();
    }

    @Override
    protected BaseMappingRegistration getEntitiesMappings() {
        MetricsMappingRegistration retVal = new MetricsMappingRegistration();
        return retVal;
    }

    @Override
    protected List<Job> getActiveJobs() {
        List<Job> jobs = new ArrayList<>();
        return jobs;
    }

    @Override
    protected void postApplicationStart() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        MetricsAppContext.getInstance().setElasticSearchService(new ElasticSearchService());
        executePostStartupCommands();
    }

    private void executePostStartupCommands() throws ApplicationRequirementsException {
        //initElasticClient();
    }

    //todo tal oyar - we initilize in BaseElasticSearchService
    private void initElasticClient() {
        MetricsConfiguration configuration = MetricsAppContext.getInstance().getConfiguration();
        ElasticConfig        elastic       = configuration.getElastic();

        String  clusterScheme     = elastic.getScheme();
        String  clusterHost       = elastic.getHost();
        Integer clusterPort       = elastic.getPort();
        String  clusterName       = elastic.getClusterName();
        Integer connectionTimeout = elastic.getConnectionTimeout();
        Integer socketTimeout     = elastic.getSocketTimeout();
        String  username          = elastic.getUsername();
        String  password          = elastic.getPassword();
        String url = String.format("%s://%s:%d", clusterScheme, clusterHost, clusterPort);

        //todo tal oyar - sniffing is not supported in the new client?
        Boolean sniff = elastic.getTransportSniff() != null ? elastic.getTransportSniff() : TRANSPORT_SNIFF_DEFAULT;

        // Initialize the elasticClient here
        //todo tal - seems like the response timeout is the socket timeout? no clear documentation on this im not sure.
        RequestConfig.Builder requestConfigBuilder =
                RequestConfig.custom().setConnectionRequestTimeout(Timeout.of(connectionTimeout, TimeUnit.MILLISECONDS))
                             .setResponseTimeout(socketTimeout, TimeUnit.MILLISECONDS);

        CloseableHttpAsyncClient httpclient =
                HttpAsyncClients.custom().setDefaultRequestConfig(requestConfigBuilder.build()).build();

        Rest5Client rest5Client = Rest5Client.builder(URI.create(url)).setHttpClient(httpclient).build();

        Rest5ClientTransport rest5ClientTransport = new Rest5ClientTransport(rest5Client, new JacksonJsonpMapper());
        ElasticsearchClient  elasticsearchClient  = new ElasticsearchClient(rest5ClientTransport);

        MetricsAppContext.getInstance().setElasticClient(elasticsearchClient);
    }
}
