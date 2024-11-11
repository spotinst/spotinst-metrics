package com.spotinst.metrics.dal.models.elastic;

/**
 * Created by Tal.Geva on 08/08/24.
 */
public class ElasticMetric {
    private String name;
    private Double value;
    private String unit;
    private Long   count;

    public ElasticMetric() {}

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

