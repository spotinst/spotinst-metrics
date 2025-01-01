package com.spotinst.metrics.commons.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
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

}
