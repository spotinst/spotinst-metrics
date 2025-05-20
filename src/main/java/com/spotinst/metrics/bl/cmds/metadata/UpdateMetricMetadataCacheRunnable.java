//package com.spotinst.metrics.bl.cmds.metadata;
//
//import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
//import com.spotinst.metrics.bl.model.request.BlMetricReportRequest;
//todo tal oyar - do we need this?
//public class UpdateMetricMetadataCacheRunnable extends BaseRunnableExecutor {
//
//    private String                accountId;
//    private BlMetricReportRequest request;
//
//    public UpdateMetricMetadataCacheRunnable(String accountId, BlMetricReportRequest request) {
//        this.accountId = accountId;
//        this.request = request;
//    }
//
//    @Override
//    protected void innerRun() {
//        UpdateMetricMetadataCacheCmd cmd = new UpdateMetricMetadataCacheCmd();
//        cmd.execute(accountId, request);
//    }
//}