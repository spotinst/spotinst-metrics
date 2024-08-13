package com.spotinst.metrics.dal.models.elastic;

public class ElasticMetricStatistics {
    private Double count;
    private Double sum;
    private Double average;
    private Double minimum;
    private Double maximum;

    public ElasticMetricStatistics() {}

    public ElasticMetricStatistics(Double count, Double sum, Double average, Double minimum, Double maximum) {
        this.count = count;
        this.sum = sum;
        this.average = average;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
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
}
