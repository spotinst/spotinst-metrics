package com.spotinst.metrics.dal.models.elastic;

/**
 * Created by zachi.nachshon on 1/18/17.
 */
public class ElasticMetricResultStatus {
    private Integer code;
    private String  name;

    public ElasticMetricResultStatus() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
