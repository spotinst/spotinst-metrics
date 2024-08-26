package com.spotinst.metrics.commons.threadpool;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import com.spotinst.metrics.MetricsAppContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportMetricsCmdExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetricsCmdExecutor.class);

    private static Integer THREAD_POOL_SIZE;

    @Getter
    private static ReportMetricsCmdExecutor instance = new ReportMetricsCmdExecutor();

    private ExecutorService executor;

    private ReportMetricsCmdExecutor() {
        Integer reportMetricsThreadPoolSize = MetricsAppContext.getInstance()
                                                               .getConfiguration()
                                                               .getIndexConfig()
                                                               .getReportMetricThreadPoolSize();

        THREAD_POOL_SIZE = reportMetricsThreadPoolSize != null ? reportMetricsThreadPoolSize : 4;

        LOGGER.info(String.format("Initializing ReportMetricsCmdExecutor thread pool with [%s] threads", THREAD_POOL_SIZE));

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
