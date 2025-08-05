package com.spotinst.metrics;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.transport.BackoffPolicy;
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
import com.spotinst.metrics.bl.managers.ElasticSearchIndexTemplateManager;
import com.spotinst.metrics.commons.configuration.BackoffPolicyConfig;
import com.spotinst.metrics.commons.configuration.BulkProcessorIndexerConfig;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import io.dropwizard.jobs.Job;
import io.dropwizard.servlets.tasks.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
        executePostStartupCommands();
    }

    private void executePostStartupCommands() throws Exception {
        //IMPORTANT - Order is important here, elastic client must be initialized first!!!
        initElasticsearchClient();
        initBulkIngester();
        handleIndexTemplatesCreation();
    }

    private void handleIndexTemplatesCreation() throws Exception {
        ElasticSearchIndexTemplateManager templateManager = new ElasticSearchIndexTemplateManager();
        templateManager.start();
    }

    private void initBulkIngester() {
        MetricsConfiguration       configuration     = MetricsAppContext.getInstance().getConfiguration();
        BulkProcessorIndexerConfig bulkIndexerConfig = configuration.getElastic().getBulkProcessorIndexer();
        ElasticsearchClient        elasticClient     = MetricsAppContext.getInstance().getElasticClient();

        Integer             bulkActions            = bulkIndexerConfig.getBufferedActions();
        Integer             bulkConcurrentRequests = bulkIndexerConfig.getConcurrentRequests();
        BackoffPolicyConfig backoffPolicyConfig    = bulkIndexerConfig.getBackoffPolicyConfig();

        Long flushIntervalInSeconds = bulkIndexerConfig.getFlushIntervalInSeconds();

        Long sizeInMB = bulkIndexerConfig.getSizeInMB();

        Long    initialDelayMs = backoffPolicyConfig.getInitialDelayMs();
        Integer numOfRetries   = backoffPolicyConfig.getNumOfRetries();

        BulkIngester<Object> bulkIngester = BulkIngester.of(b -> b.client(elasticClient).maxOperations(bulkActions)
                                                                  .flushInterval(flushIntervalInSeconds,
                                                                                 TimeUnit.SECONDS).backoffPolicy(
                        BackoffPolicy.exponentialBackoff(initialDelayMs, numOfRetries))
                                                                  .maxConcurrentRequests(bulkConcurrentRequests)
                                                                  .listener(new BulkListener<>() {

                                                                      @Override
                                                                      public void beforeBulk(long executionId,
                                                                                             BulkRequest request,
                                                                                             List<Object> contexts) {
                                                                          int                 preSaveDocsSize = -1;
                                                                          List<BulkOperation> preSaveDocs     =
                                                                                  request.operations();

                                                                          if (preSaveDocs != null) {
                                                                              preSaveDocsSize = preSaveDocs.size();
                                                                          }

                                                                          LOGGER.info(String.format(
                                                                                  "Start indexing [%s] metric documents.",
                                                                                  preSaveDocsSize));
                                                                      }

                                                                      @Override
                                                                      public void afterBulk(long l, BulkRequest request,
                                                                                            List<Object> list,
                                                                                            BulkResponse response) {
                                                                          int postSaveDocsSize = -1;

                                                                          if (response.items() != null) {
                                                                              postSaveDocsSize =
                                                                                      response.items().size();
                                                                          }

                                                                          long tookInMillis         = response.took();
                                                                          long estimatedSizeInBytes =
                                                                                  estimateBulkRequestSize(request);

                                                                          String sizeStr =
                                                                                  estimatedSizeInBytes > 1000000 ?
                                                                                  String.format("%s%s",
                                                                                                estimatedSizeInBytes /
                                                                                                1000000, " MB") :
                                                                                  String.format("%s%s",
                                                                                                estimatedSizeInBytes,
                                                                                                " Bytes");

                                                                          LOGGER.info(
                                                                                  "Finished indexing {} documents in {}ms, Size={}",
                                                                                  postSaveDocsSize, tookInMillis,
                                                                                  sizeStr);
                                                                          ;
                                                                      }

                                                                      @Override
                                                                      public void afterBulk(long l, BulkRequest request,
                                                                                            List<Object> list,
                                                                                            Throwable failure) {
                                                                          int                 failedDocsSize = -1;
                                                                          List<BulkOperation> failedDocs     =
                                                                                  request.operations();

                                                                          if (failedDocs != null) {
                                                                              failedDocsSize = failedDocs.size();
                                                                          }

                                                                          LOGGER.error(
                                                                                  "Failed indexing {} metric documents. Errors: {}",
                                                                                  failedDocsSize, failure.getMessage(),
                                                                                  failure);
                                                                      }
                                                                  }));

        MetricsAppContext.getInstance().setBulkIngester(bulkIngester);

        printBulkProcessorSettings(bulkActions, sizeInMB, flushIntervalInSeconds, bulkConcurrentRequests);
    }

    private void initElasticsearchClient() {
        LOGGER.info("Starting elastic search ElasticsearchClient initialization...");

        ElasticConfig config = MetricsAppContext.getInstance().getConfiguration().getElastic();

        String  host     = config.getHost();
        Integer port     = config.getPort();
        String  scheme   = config.getScheme();
        String  userName = config.getUsername();
        String  password = config.getPassword();

        if (StringUtils.isEmpty(host) || port == null || StringUtils.isEmpty(scheme)) {
            String errMsg = "Cannot create elastic search ElasticsearchClient, host, port or scheme is missing!";
            LOGGER.error(errMsg);
            throw new ApplicationRequirementsException(errMsg);
        }

        try {
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(host, port),
                                               new UsernamePasswordCredentials(userName, password.toCharArray()));

            String url = String.format("%s://%s:%d", scheme, host, port);

            ElasticsearchClient elasticsearchClient =
                    ElasticsearchClient.of(cb -> cb.host(url).usernameAndPassword(userName, password));
            MetricsAppContext.getInstance().setElasticClient(elasticsearchClient);

            LOGGER.info("Finished elastic search ElasticsearchClient initialization.");
        }
        catch (Exception e) {
            String errMsg = "Error when trying to initialize Elastic search client.";
            LOGGER.error(errMsg, e);
            throw new ApplicationRequirementsException(errMsg);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                MetricsAppContext.getInstance().getElasticClient().close();
            }
            catch (IOException e) {
                LOGGER.error("Error when trying to close Elastic search client.", e);
            }
        }));
    }

    private void printBulkProcessorSettings(Integer bulkActions, Long bulkSize, Long bulkInterval,
                                            Integer bulkConcurrentRequests) {
        String sb =
                "\n\n" + "ElasticSearch Bulk Processor Flush Configuration:" + "\n  " + "Number of requests" + " : " +
                bulkActions + "\n  " + "Size of requests" + " : " + bulkSize + "\n  " + "Flush interval" + " : " +
                bulkInterval + "\n  " + "Concurrent request execution" + " : " + bulkConcurrentRequests + "\n";

        LOGGER.info(sb);
    }

    // Custom method to estimate the size of a bulk request
    private long estimateBulkRequestSize(BulkRequest request) {
        return request.operations().stream().mapToLong(op -> op.toString().getBytes(StandardCharsets.UTF_8).length)
                      .sum();
    }
}