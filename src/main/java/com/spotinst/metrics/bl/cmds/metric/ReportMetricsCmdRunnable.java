package com.spotinst.metrics.bl.cmds.metric;

import com.spotinst.dropwizard.common.executors.BaseRunnableExecutor;
import com.spotinst.metrics.api.model.request.ApiMetricsReportRequest;
import com.spotinst.metrics.bl.model.request.BlMetricReportRequest;
import com.spotinst.metrics.commons.converters.MetricReportConverter;

public class ReportMetricsCmdRunnable extends BaseRunnableExecutor {

    private ApiMetricsReportRequest request;
    private String                  index;

    public ReportMetricsCmdRunnable(ApiMetricsReportRequest request, String index) {
        this.request = request;
        this.index = index;
    }

    @Override
    protected void innerRun() {
        BlMetricReportRequest blRequest = MetricReportConverter.apiToBl(request);
        ReportMetricCmd       cmd       = new ReportMetricCmd();
        cmd.execute(blRequest, index);
    }
}