package com.spotinst.metrics.dal.services.elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.BackoffPolicy;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.BackoffPolicyConfig;
import com.spotinst.metrics.commons.configuration.BulkProcessorIndexerConfig;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;


public abstract class BaseElasticSearchService implements IElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticSearchService.class);

    protected ElasticsearchClient elasticsearchClient;
    protected BulkIngester        bulkIngester;

    public BaseElasticSearchService() {
        initElasticsearchClient();
        initBulkIngester();
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
            String errMsg = "Cannot create elastic search ElasticsearchClient, host is missing!";
            LOGGER.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        try {
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(host, port),
                                               new UsernamePasswordCredentials(userName, password.toCharArray()));

            String url = String.format("%s://%s:%d", scheme, host, port);

            //todo tal - seems like the response timeout is the socket timeout? no clear documentation on this im not sure.
            Timeout               responseTimeout      = Timeout.of(30, TimeUnit.SECONDS);
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

            CloseableHttpAsyncClient httpclient =
                    HttpAsyncClients.custom().setDefaultCredentialsProvider(credentialsProvider)
                                    .setDefaultRequestConfig(requestConfigBuilder.build()).build();

            Rest5Client rest5Client = Rest5Client.builder(URI.create(url)).setHttpClient(httpclient).build();

            Rest5ClientTransport rest5ClientTransport = new Rest5ClientTransport(rest5Client, new JacksonJsonpMapper());

            ElasticsearchClient client = new ElasticsearchClient(rest5ClientTransport);

            //todo tal - Need to verify if we want to set timeouts for the requests/responses, if so is it possible using the new initialize method
            ElasticsearchClient elasticsearchClient1 =
                    ElasticsearchClient.of(cb -> cb.host(url).usernameAndPassword(userName, password));

            this.elasticsearchClient = elasticsearchClient1;

            LOGGER.info("Finished elastic search ElasticsearchClient initialization...");
        }
        catch (Exception e) {
            LOGGER.error("Error when trying to initialize Elastic search client.", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.elasticsearchClient.close();
            }
            catch (IOException e) {
                LOGGER.error("Error when trying to close Elastic search client.", e);
            }
        }));
    }

    private void initBulkIngester() {
        if (elasticsearchClient == null) {
            return;
        }

        MetricsConfiguration       configuration     = MetricsAppContext.getInstance().getConfiguration();
        BulkProcessorIndexerConfig bulkIndexerConfig = configuration.getElastic().getBulkProcessorIndexer();

        Integer             bulkActions            = bulkIndexerConfig.getBufferedActions();
        Integer             bulkConcurrentRequests = bulkIndexerConfig.getConcurrentRequests();
        BackoffPolicyConfig backoffPolicyConfig    = bulkIndexerConfig.getBackoffPolicyConfig();

        Long      flushIntervalInSeconds = bulkIndexerConfig.getFlushIntervalInSeconds();
        TimeValue bulkInterval           = TimeValue.ofSeconds(flushIntervalInSeconds);

        Long sizeInMB = bulkIndexerConfig.getSizeInMB();

        Long    initialDelayMs = backoffPolicyConfig.getInitialDelayMs();
        Integer numOfRetries   = backoffPolicyConfig.getNumOfRetries();

        this.bulkIngester = BulkIngester.of(
                b -> b.client(elasticsearchClient).maxOperations(bulkActions).flushInterval(5, TimeUnit.SECONDS)
                      .backoffPolicy(BackoffPolicy.exponentialBackoff(initialDelayMs, numOfRetries))
                      .maxConcurrentRequests(bulkConcurrentRequests).listener(new BulkListener<>() {

                            @Override
                            public void beforeBulk(long executionId, BulkRequest request, List<Object> contexts) {
                                int                 preSaveDocsSize = -1;
                                List<BulkOperation> preSaveDocs     = request.operations();

                                if (preSaveDocs != null) {
                                    preSaveDocsSize = preSaveDocs.size();
                                }
                                LOGGER.info(String.format("Start indexing [%s] metric documents...", preSaveDocsSize));
                            }

                            @Override
                            public void afterBulk(long l, BulkRequest request, List<Object> list,
                                                  BulkResponse response) {
                                int postSaveDocsSize = -1;
                                if (response.items() != null) {
                                    postSaveDocsSize = response.items().size();
                                }

                                long tookInMillis         = response.took();
                                long estimatedSizeInBytes = estimateBulkRequestSize(request);

                                String sizeStr = estimatedSizeInBytes > 1000000 ?
                                                 String.format("%s%s", estimatedSizeInBytes / 1000000, " MB") :
                                                 String.format("%s%s", estimatedSizeInBytes, " Bytes");

                                String format = "Finished indexing [%s] documents in %sms, Size=%s";

                                String msg = String.format(format, postSaveDocsSize, tookInMillis, sizeStr);
                                LOGGER.info(msg);
                            }

                            @Override
                            public void afterBulk(long l, BulkRequest request, List<Object> list, Throwable failure) {
                                int                 failedDocsSize = -1;
                                List<BulkOperation> failedDocs     = request.operations();

                                if (failedDocs != null) {
                                    failedDocsSize = failedDocs.size();
                                }

                                LOGGER.error("Failed indexing {} metric documents. Errors: {}", failedDocsSize,
                                             failure.getMessage(), failure);
                            }
                        }));

        printBulkProcessorSettings(bulkActions, sizeInMB, bulkInterval, bulkConcurrentRequests);
    }

    private void printBulkProcessorSettings(Integer bulkActions, Long bulkSize, TimeValue bulkInterval,
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