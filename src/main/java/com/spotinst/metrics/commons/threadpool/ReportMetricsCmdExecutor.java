package com.spotinst.metrics.commons.threadpool;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportMetricsCmdExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetricsCmdExecutor.class);

    @Getter
    private static ReportMetricsCmdExecutor instance = new ReportMetricsCmdExecutor();

    private ExecutorService executor;

    private ReportMetricsCmdExecutor() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void execute(BaseRunnableExecutor workerThread) {

        Thread.ofVirtual().start(() -> {
            executor.execute(workerThread);
        });
    }
}