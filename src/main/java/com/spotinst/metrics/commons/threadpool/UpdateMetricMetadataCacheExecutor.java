package com.spotinst.metrics.commons.threadpool;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import com.spotinst.metrics.MetricsAppContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateMetricMetadataCacheExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMetricMetadataCacheExecutor.class);

    private static Integer THREAD_POOL_SIZE;

    @Getter
    private static UpdateMetricMetadataCacheExecutor instance = new UpdateMetricMetadataCacheExecutor();

    private ExecutorService executor;

    private UpdateMetricMetadataCacheExecutor() {
        Integer reportMetricsThreadPoolSize = MetricsAppContext.getInstance()
                                                               .getConfiguration()
                                                               .getMetadataCache()
                                                               .getUpdateCacheThreadPoolSize();

        THREAD_POOL_SIZE = reportMetricsThreadPoolSize != null ? reportMetricsThreadPoolSize : 4;

        LOGGER.info(String.format("Initializing UpdateMetricMetadataCacheExecutor thread pool with [%s] threads", THREAD_POOL_SIZE));

        // Initialize thread pool
        initThreadPool();
    }

    private void initThreadPool() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void execute(BaseRunnableExecutor workerThread) {
        executor.execute(workerThread);
    }
}
