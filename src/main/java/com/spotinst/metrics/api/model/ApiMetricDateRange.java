package com.spotinst.metrics.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by Tal.Geva on 07/08/2024.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetricDateRange {

    @NotNull
    private Date from;

    @NotNull
    private Date to;

    public ApiMetricDateRange() {
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }
}
