package com.spotinst.metrics.dal.models.elastic;

import java.util.Date;

public class ElasticMetricDateRange {
    private Date from;
    private Date to;

    public ElasticMetricDateRange() {
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
