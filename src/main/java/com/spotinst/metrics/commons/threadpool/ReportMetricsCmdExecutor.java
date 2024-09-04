package com.spotinst.metrics.commons.threadpool;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import com.spotinst.metrics.MetricsAppContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class ReportMetricsCmdExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetricsCmdExecutor.class);

    @Getter
    private static ReportMetricsCmdExecutor instance = new ReportMetricsCmdExecutor();

    private ExecutorService executor;

    private ReportMetricsCmdExecutor() {
    }

    public void execute(BaseRunnableExecutor workerThread) {

        Thread.ofVirtual().start(()-> {
            executor.execute(workerThread);
        });
    }
}