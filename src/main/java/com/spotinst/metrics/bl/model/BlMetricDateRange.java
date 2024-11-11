package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class BlMetricDateRange {
    private Date from;
    private Date to;

    public BlMetricDateRange() {}

    @Override
    public String toString() {
        return "BlMetricDateRange{" + "from=" + from + ", to=" + to + '}';
    }
}
