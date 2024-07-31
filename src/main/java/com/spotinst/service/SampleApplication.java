package com.spotinst.service;

import com.spotinst.commons.exceptions.ApplicationRequirementsException;
import com.spotinst.commons.mapper.entities.registration.BaseMappingRegistration;
import com.spotinst.dropwizard.bl.notification.INotificationHandler;
import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.dropwizard.common.filters.auth.token.TokenPayLoad;
import com.spotinst.dropwizard.common.ratelimit.*;
import com.spotinst.dropwizard.common.ratelimit.RateLimiterAggregator.Interval;
import com.spotinst.dropwizard.common.service.BaseApplication;
import com.spotinst.dropwizard.common.service.healthcheck.HelloWorldResource;
import com.spotinst.messaging.SpotinstSqsMessagingBuilder;
import com.spotinst.messaging.clients.consumer.IMessagingConsumer;
import com.spotinst.messaging.clients.producer.IMessagingProducer;
import com.spotinst.messaging.configuration.sqs.ProducerConfig.ProducerType;
import com.spotinst.service.api.filters.InboundOutboundFeature;
import com.spotinst.service.api.filters.auth.InboundAuthFeature;
import com.spotinst.service.api.resources.app.SampleResource;
import com.spotinst.service.api.resources.tasks.ReloadOrganizationsTask;
import com.spotinst.service.api.resources.tasks.StartConsumerTask;
import com.spotinst.service.api.resources.tasks.StopConsumerTask;
import com.spotinst.service.bl.cmds.lookups.LoadOrganizationsCmd;
import com.spotinst.service.bl.jobs.LoadOrganizationsJob;
import com.spotinst.service.commons.configuration.SampleConfiguration;
import com.spotinst.service.commons.configuration.ratelimit.OrganizationToMySqlMapper;
import com.spotinst.service.messaging.consumer.SampleMessagingConsumer;
import com.spotinst.service.messaging.producer.handler.NotificationMessagingHandler;
import io.dropwizard.jobs.Job;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.core.setup.Bootstrap;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class SampleApplication extends BaseApplication<SampleConfiguration, TokenPayLoad> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApplication.class);

    // region Entry Point
    public static void main(String[] args) throws Exception {
        new SampleApplication().run(args);
    }
    // endregion

    @Override
    public String getName() {
        return "java-service-template";
    }

    @Override
    protected List<Object> getApiResources() {
        List<Object> apiResources = new ArrayList<>();
        // ALWAYS register resources with Class.class, or you will have ResourceContext == null
        apiResources.add(HelloWorldResource.class);
        apiResources.add(SampleResource.class);
        return apiResources;
    }

    @Override
    protected List<Task> getAdminTasks() {
        List<Task> tasks = new ArrayList<>();

        // Messaging Tasks
        tasks.add(new StartConsumerTask());
        tasks.add(new StopConsumerTask());

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
        return SampleAppContext.getInstance();
    }

    @Override
    protected BaseMappingRegistration getEntitiesMappings() {
        ServiceMappingRegistration retVal = new ServiceMappingRegistration();
        return retVal;
    }

    @Override
    protected List<Job> getActiveJobs() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(new LoadOrganizationsJob());
        return jobs;
    }

    @Override
    protected INotificationHandler getNotificationHandler() {
        //
        // Messaging based notification
        //
        IMessagingProducer sqsProducer = SampleAppContext.getInstance().getSqsProducer();
        return new NotificationMessagingHandler(sqsProducer);

        //
        // HTTP based notification
        //
        // String eventsServiceUrl      = configuration.getServices().getEventsServiceUrl();
        // String spotinstInternalToken = configuration.getSpotinstInternalToken();
        // return new NotificationHttpHandler(eventsServiceUrl, spotinstInternalToken);
    }

    @Override
    protected void preApplicationStart(Bootstrap<SampleConfiguration> bootstrap) {
        LOGGER.info("Configuring swagger before application startup");

        bootstrap.addBundle(new SwaggerBundle<SampleConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(SampleConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    protected void postApplicationStart() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        executePostStartupCommands();
        initSqsMessaging();
        initRateLimiter();
    }

    private void initRateLimiter() {
        RateLimitConfig rateLimitConfig = configuration.getRateLimit();
        SpotinstRateLimiter.getInstance().init(rateLimitConfig, new OrganizationToMySqlMapper());
        SpotinstRateLimiter.getInstance()
                           .registerAggregator(new RateLimiterAggregator(Interval.MINUTE, getServiceContext()));
    }

    private void initSqsMessaging() {
        SampleConfiguration configuration = SampleAppContext.getInstance().getConfiguration();
        if (configuration.getMessaging() != null) {
            SpotinstSqsMessagingBuilder mb = new SpotinstSqsMessagingBuilder(configuration.getMessaging());

            if (configuration.getMessaging().getProducerConfig().getSqs() != null) {
                IMessagingProducer sqsProducer = mb.producer(ProducerType.SQS).build();
                SampleAppContext.getInstance().setSqsProducer(sqsProducer);
            }

            if (configuration.getMessaging().getProducerConfig().getSns() != null) {
                IMessagingProducer snsProducer = mb.producer(ProducerType.SNS).build();
                SampleAppContext.getInstance().setSnsProducer(snsProducer);
            }

            if (configuration.getMessaging().getConsumerConfig() != null) {
                IMessagingConsumer sqsConsumer = mb.consumer(SampleMessagingConsumer.class).build();
                SampleAppContext.getInstance().setSqsConsumer(sqsConsumer);
                sqsConsumer.consume();
            }
        }
    }

    private void executePostStartupCommands() throws ApplicationRequirementsException {
        LoadOrganizationsCmd loadOrgCmd = new LoadOrganizationsCmd();
        loadOrgCmd.execute();

        Boolean isOrgLoaded = loadOrgCmd.getResult();
        if (isOrgLoaded == false) {
            throw new ApplicationRequirementsException("Failed loading organization at service startup");
        }
    }
}
