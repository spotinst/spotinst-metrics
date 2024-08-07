package com.spotinst.metrics;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.base.Supplier;
import com.spotinst.commons.exceptions.ApplicationRequirementsException;
import com.spotinst.commons.mapper.entities.registration.BaseMappingRegistration;
import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.dropwizard.common.filters.auth.token.TokenPayLoad;
import com.spotinst.dropwizard.common.ratelimit.*;
import com.spotinst.dropwizard.common.ratelimit.RateLimiterAggregator.Interval;
import com.spotinst.dropwizard.common.service.BaseApplication;
import com.spotinst.dropwizard.common.service.healthcheck.HelloWorldResource;
import com.spotinst.metrics.api.filters.InboundOutboundFeature;
import com.spotinst.metrics.api.filters.auth.InboundAuthFeature;
import com.spotinst.metrics.api.resources.app.MetricsResource;
import com.spotinst.metrics.api.resources.tasks.ReloadOrganizationsTask;
import com.spotinst.metrics.bl.cmds.lookups.LoadOrganizationsCmd;
import com.spotinst.metrics.bl.jobs.LoadOrganizationsJob;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import com.spotinst.metrics.commons.configuration.ratelimit.OrganizationToMySqlMapper;
import io.dropwizard.jobs.Job;
import io.dropwizard.servlets.tasks.Task;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MetricsApplication extends BaseApplication<MetricsConfiguration, TokenPayLoad> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsApplication.class);

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

    //TODO Tal: Maybe I should return null
    @Override
    protected List<Task> getAdminTasks() {
        List<Task> tasks = new ArrayList<>();

        // Admin Tasks
        tasks.add(new ReloadOrganizationsTask());

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
        jobs.add(new LoadOrganizationsJob());
        return jobs;
    }

    @Override
    protected void postApplicationStart() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        executePostStartupCommands();
        initRateLimiter();
    }

    private void initRateLimiter() {
        RateLimitConfig rateLimitConfig = configuration.getRateLimit();
        SpotinstRateLimiter.getInstance().init(rateLimitConfig, new OrganizationToMySqlMapper());
        SpotinstRateLimiter.getInstance()
                           .registerAggregator(new RateLimiterAggregator(Interval.MINUTE, getServiceContext()));
    }

    private void executePostStartupCommands() throws ApplicationRequirementsException {
        LoadOrganizationsCmd loadOrgCmd = new LoadOrganizationsCmd();
        loadOrgCmd.execute();

        Boolean isOrgLoaded = loadOrgCmd.getResult();
        if (isOrgLoaded == false) {
            throw new ApplicationRequirementsException("Failed loading organization at service startup");
        }
        initElasticClient();
    }

    private void initElasticClient() {
        MetricsConfiguration configuration = MetricsAppContext.getInstance().getConfiguration();
        ElasticConfig        elastic       = configuration.getElastic();

        String  clusterScheme     = elastic.getClusterScheme();
        String  clusterHost       = elastic.getClusterHost();
        Integer clusterPort       = elastic.getClusterPort();
        Integer connectionTimeout = elastic.getConnectionTimeout();
        Integer socketTimeout     = elastic.getSocketTimeout();

        //sign with aws signature for internal security communication
        Supplier<LocalDateTime> clock               = () -> LocalDateTime.now(ZoneOffset.UTC);
        AWSCredentialsProvider  credentialsProvider = new DefaultAWSCredentialsProviderChain();


        AWSSigner signer = new AWSSigner(credentialsProvider, elastic.getClusterRegion(), "es", clock);

        HttpRequestInterceptor interceptor = new AWSSigningRequestInterceptor(signer);

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(clusterHost, clusterPort, clusterScheme))
                          .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor))
                          .setRequestConfigCallback(
                                  requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(connectionTimeout)
                                                                              .setSocketTimeout(socketTimeout)));

        MetricsAppContext.getInstance().setElasticClient(client);
    }
}
