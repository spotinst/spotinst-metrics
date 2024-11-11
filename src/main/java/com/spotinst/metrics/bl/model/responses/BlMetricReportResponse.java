package com.spotinst.metrics.bl.model.responses;

import com.spotinst.metrics.bl.model.BlMetricResultStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricReportResponse {
    private BlMetricResultStatus status;

    public BlMetricReportResponse() {
    }

}
