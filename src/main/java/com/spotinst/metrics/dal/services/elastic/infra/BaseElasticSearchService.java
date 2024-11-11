package com.spotinst.metrics.dal.services.elastic.infra;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.MetricsConfiguration;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseElasticSearchService implements IElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticSearchService.class);

    protected BulkProcessor bulkProcessor;
    public BaseElasticSearchService() {
//        initBulkProcessor();
    }

//    private void initBulkProcessor() {
//        RestHighLevelClient client = MetricsAppContext.getInstance().getElasticClient();
//
//        if(client == null) {
//            return;
//        }
//
//        MetricsConfiguration       configuration = MetricsAppContext.getInstance().getConfiguration();
//        BulkProcessorIndexerConfig indexerConfig = configuration.getElasticsearch().getBulkProcessorIndexer();
//
//        Integer             bulkActions            = indexerConfig.getBufferedActions();
//        Integer             bulkConcurrentRequests = indexerConfig.getConcurrentRequests();
//        BackoffPolicyConfig backoffPolicyConfig    = indexerConfig.getBackoffPolicyConfig();
//
//        Long      flushIntervalInSeconds = indexerConfig.getFlushIntervalInSeconds();
//        TimeValue bulkInterval           = TimeValue.timeValueSeconds(flushIntervalInSeconds);
//
//        Long          sizeInMB = indexerConfig.getSizeInMB();
//        ByteSizeValue bulkSize = new ByteSizeValue(sizeInMB, ByteSizeUnit.MB);
//
//        Long    initialDelayMs = backoffPolicyConfig.getInitialDelayMs();
//        Integer numOfRetries   = backoffPolicyConfig.getNumOfRetries();
//
//        this.bulkProcessor = BulkProcessor.builder(transportClient,
//                                                   new BulkProcessor.Listener() {
//                                                       @Override
//                                                       public void beforeBulk(long executionId, BulkRequest request) {
//                                                           int                 preSaveDocsSize = -1;
//                                                           List<ActionRequest> preSaveDocs     = request.requests();
//                                                           if(preSaveDocs != null) {
//                                                               preSaveDocsSize = preSaveDocs.size();
//                                                           }
//                                                           LOGGER.info(String.format("Start indexing [%s] metric documents...", preSaveDocsSize));
//                                                       }
//
//                                                       @Override
//                                                       public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                                                           int postSaveDocsSize = -1;
//                                                           if(response.getItems() != null) {
//                                                               postSaveDocsSize = response.getItems().length;
//                                                           }
//
//                                                           long tookInMillis         = response.getTookInMillis();
//                                                           long estimatedSizeInBytes = request.estimatedSizeInBytes();
//
//                                                           String sizeFormat = "%s%s";
//                                                           String sizeStr = estimatedSizeInBytes > 1000000 ?
//                                                                            String.format(sizeFormat, estimatedSizeInBytes / 1000000, " MB") :
//                                                                            String.format(sizeFormat, estimatedSizeInBytes, " Bytes");
//
//                                                           String format = "Finished indexing [%s] documents in %sms, Size=%s";
//                                                           String msg = String.format(format, postSaveDocsSize, tookInMillis, sizeStr);
//                                                           LOGGER.info(msg);
//                                                       }
//
//                                                       @Override
//                                                       public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
//                                                           int failedDocsSize = -1;
//                                                           List<ActionRequest> failedDocs = request.requests();
//                                                           if(failedDocs != null) {
//                                                               failedDocsSize = failedDocs.size();
//                                                           }
//                                                           LOGGER.error(String.format("Failed indexing [%s] metric documents", failedDocsSize), failure);
//                                                       }
//                                                   })
//                                          .setBulkActions(bulkActions)
//                                          .setBulkSize(bulkSize)
//                                          .setFlushInterval(bulkInterval)
//                                          .setConcurrentRequests(bulkConcurrentRequests)
//                                          .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(initialDelayMs), numOfRetries))
//                                          .build();
//
//        printBulkProcessorSettings(bulkActions, bulkSize, bulkInterval, bulkConcurrentRequests);
//    }
}
