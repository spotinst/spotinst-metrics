package com.spotinst.metrics.bl.model.request;

import com.spotinst.metrics.bl.model.BlMetricDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BlMetricReportRequest {
    private List<BlMetricDocument> metricDocuments;

    public BlMetricReportRequest() {
    }
}
