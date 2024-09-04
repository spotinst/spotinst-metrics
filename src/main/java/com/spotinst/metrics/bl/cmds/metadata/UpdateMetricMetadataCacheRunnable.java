package com.spotinst.metrics.bl.cmds.metadata;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import com.spotinst.metrics.bl.model.BlMetricReportRequest;

/**
 * Created by zachi.nachshon on 3/19/17.
 */
public class UpdateMetricMetadataCacheRunnable extends BaseRunnableExecutor {

    private String                accountId;
    private BlMetricReportRequest request;

    public UpdateMetricMetadataCacheRunnable(String accountId, BlMetricReportRequest request) {
        this.accountId = accountId;
        this.request = request;
    }

    @Override
    protected void innerRun() {
        UpdateMetricMetadataCacheCmd cmd = new UpdateMetricMetadataCacheCmd();
        cmd.execute(accountId, request);
    }
}