package com.spotinst.metrics.dal.services.elastic.infra;

import com.spotinst.metrics.MetricsAppContext;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import com.spotinst.metrics.commons.configuration.BackoffPolicyConfig;
import com.spotinst.metrics.commons.configuration.BulkProcessorIndexerConfig;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by zachi.nachshon on 1/17/17.
 */
public abstract class BaseElasticSearchService implements IElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticSearchService.class);

    protected RestHighLevelClient restHighLevelClient;
    protected BulkProcessor       bulkProcessor;

    public BaseElasticSearchService() {
        initRestHighLevelClient();
        initBulkProcessor();
    }

    private void initBulkProcessor() {
        if (restHighLevelClient == null) {
            return;
        }

        MetricsConfiguration       configuration = MetricsAppContext.getInstance().getConfiguration();
        BulkProcessorIndexerConfig indexerConfig = configuration.getElastic().getBulkProcessorIndexer();

        Integer             bulkActions            = indexerConfig.getBufferedActions();
        Integer             bulkConcurrentRequests = indexerConfig.getConcurrentRequests();
        BackoffPolicyConfig backoffPolicyConfig    = indexerConfig.getBackoffPolicyConfig();

        Long      flushIntervalInSeconds = indexerConfig.getFlushIntervalInSeconds();
        TimeValue bulkInterval           = TimeValue.timeValueSeconds(flushIntervalInSeconds);

        Long          sizeInMB = indexerConfig.getSizeInMB();
        ByteSizeValue bulkSize = new ByteSizeValue(sizeInMB, ByteSizeUnit.MB);

        Long    initialDelayMs = backoffPolicyConfig.getInitialDelayMs();
        Integer numOfRetries   = backoffPolicyConfig.getNumOfRetries();

        this.bulkProcessor = BulkProcessor.builder(
                                                  (request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                                                  new BulkProcessor.Listener() {
                                                      @Override
                                                      public void beforeBulk(long executionId, BulkRequest request) {
                                                          int                      preSaveDocsSize = -1;
                                                          List<DocWriteRequest<?>> preSaveDocs     = request.requests();
                                                          if (preSaveDocs != null) {
                                                              preSaveDocsSize = preSaveDocs.size();
                                                          }
                                                          LOGGER.info(String.format("Start indexing [%s] metric documents...", preSaveDocsSize));
                                                      }

                                                      @Override
                                                      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                                                          int postSaveDocsSize = -1;
                                                          if (response.getItems() != null) {
                                                              postSaveDocsSize = response.getItems().length;
                                                          }

                                                          long tookInMillis         = response.getTook().getMillis();
                                                          long estimatedSizeInBytes = request.estimatedSizeInBytes();

                                                          String sizeFormat = "%s%s";
                                                          String sizeStr = estimatedSizeInBytes > 1000000 ?
                                                                           String.format(sizeFormat, estimatedSizeInBytes / 1000000, " MB") :
                                                                           String.format(sizeFormat, estimatedSizeInBytes, " Bytes");

                                                          String format = "Finished indexing [%s] documents in %sms, Size=%s";
                                                          String msg    = String.format(format, postSaveDocsSize, tookInMillis, sizeStr);
                                                          LOGGER.info(msg);
                                                      }

                                                      @Override
                                                      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                                                          int                      failedDocsSize = -1;
                                                          List<DocWriteRequest<?>> failedDocs     = request.requests();
                                                          if (failedDocs != null) {
                                                              failedDocsSize = failedDocs.size();
                                                          }
                                                          LOGGER.error(String.format("Failed indexing [%s] metric documents", failedDocsSize), failure);
                                                      }
                                                  }).setBulkActions(bulkActions).setBulkSize(bulkSize).setFlushInterval(bulkInterval)
                                          .setConcurrentRequests(bulkConcurrentRequests).setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(initialDelayMs), numOfRetries))
                                          .build();

        printBulkProcessorSettings(bulkActions, bulkSize, bulkInterval, bulkConcurrentRequests);
    }

//    private void initRestHighLevelClient() {
//        LOGGER.info("Starting elastic search RestHighLevelClient initialization...");
//
//        ElasticConfig config = MetricsAppContext.getInstance().getConfiguration().getElastic();
//
//        String  host = config.getHost();
//        Integer port = config.getPort();
//        String  scheme = config.getScheme();
////        String userName = config.getUsername();
////        String password = config.getPassword();
//
//        if (StringUtils.isEmpty(host) || port == null || StringUtils.isEmpty(scheme)) {
//            String errMsg = "Cannot create elastic search RestHighLevelClient, host is missing !";
//            LOGGER.error(errMsg);
//            throw new RuntimeException(errMsg);
//        }
//
//        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));
//
//        this.restHighLevelClient = new RestHighLevelClient(builder);
//
//        LOGGER.info("Finished elastic search RestHighLevelClient initialization...");
//    }

    private void initRestHighLevelClient() {
        LOGGER.info("Starting elastic search RestHighLevelClient initialization...");

        ElasticConfig config = MetricsAppContext.getInstance().getConfiguration().getElastic();

        String host = config.getHost();
        Integer port = config.getPort();
        String scheme = config.getScheme();
        String userName = config.getUsername();
        String password = config.getPassword();

        if (StringUtils.isEmpty(host) || port == null || StringUtils.isEmpty(scheme)) {
            String errMsg = "Cannot create elastic search RestHighLevelClient, host is missing !";
            LOGGER.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                                              .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        this.restHighLevelClient = new RestHighLevelClient(builder);

        LOGGER.info("Finished elastic search RestHighLevelClient initialization...");
    }

    private void printBulkProcessorSettings(Integer bulkActions, ByteSizeValue bulkSize, TimeValue bulkInterval,
                                            Integer bulkConcurrentRequests) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        sb.append("ElasticSearch Bulk Processor Flush Configuration:");
        sb.append("\n  ").append("Number of requests").append(" : ").append(bulkActions);
        sb.append("\n  ").append("Size of requests").append(" : ").append(bulkSize);
        sb.append("\n  ").append("Flush interval").append(" : ").append(bulkInterval);
        sb.append("\n  ").append("Concurrent request execution").append(" : ").append(bulkConcurrentRequests);
        sb.append("\n");

        LOGGER.info(sb.toString());
    }

    public void close() throws IOException {
        if (bulkProcessor != null) {
            bulkProcessor.close();
        }
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }
}