package com.spotinst.metrics.bl.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlMetricResultStatus {
    private Integer code;
    private String  name;

    public BlMetricResultStatus() {
    }

}