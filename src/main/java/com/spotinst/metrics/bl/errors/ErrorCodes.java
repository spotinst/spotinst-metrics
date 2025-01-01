package com.spotinst.metrics.bl.errors;

import com.spotinst.commons.errors.IErrorCode;

/**
 * Created by zachi.nachshon on 7/26/17.
 */
public enum ErrorCodes implements IErrorCode {

//    FAILED_TO_FLUSH_METRICS_METADATA("FAILED_TO_FLUSH_METRICS_METADATA"),
    INVALID_REQUEST("INVALID_REQUEST"),
    FAILED_TO_GET_METRICS_STATISTICS("FAILED_TO_GET_METRICS_STATISTICS"),
    FAILED_TO_GET_DIMENSIONS_VALUES("FAILED_TO_GET_DIMENSIONS_VALUES"),
    FAILED_TO_GET_PERCENTILES("FAILED_TO_GET_PERCENTILES");
//    FAILED_TO_GET_LAST_AGG_METRIC_TIME("FAILED_TO_GET_METRICS_STATISTICS"),
//    FAILED_TO_REPORT_METRICS_DATA("FAILED_TO_REPORT_METRICS_DATA");

    private final String reasonPhrase;

    ErrorCodes(final String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }
}
