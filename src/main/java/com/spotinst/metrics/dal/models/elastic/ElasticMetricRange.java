package com.spotinst.metrics.dal.models.elastic;

public class ElasticMetricRange {
    private Double minimum;
    private Double maximum;
    private Double gap;

    public ElasticMetricRange() {
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getGap() {
        return gap;
    }

    public void setGap(Double gap) {
        this.gap = gap;
    }

    public Boolean hasPartialData() {
        return (minimum == null || minimum.isNaN()) ||
               (maximum == null || maximum.isNaN()) ||
               (gap == null || gap.isNaN());
    }

    @Override
    public String toString() {
        return "EsMetricRange{" +
               "minimum=" + minimum +
               ", maximum=" + maximum +
               ", gap=" + gap +
               '}';
    }
}
