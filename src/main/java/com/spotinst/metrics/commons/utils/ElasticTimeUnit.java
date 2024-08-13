package com.spotinst.metrics.commons.utils;

import java.util.Date;

public class ElasticTimeUnit {
    private String from;
    private String to;

    private Date fromDate;
    private Date toDate;

    public ElasticTimeUnit(String from, String to, Date fromDate, Date toDate) {
        this.from = from;
        this.to = to;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
