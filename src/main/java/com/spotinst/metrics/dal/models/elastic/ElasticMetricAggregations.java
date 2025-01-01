package com.spotinst.metrics.dal.models.elastic;

import java.util.ArrayList;
import java.util.List;

public class ElasticMetricAggregations {
    private List<ElasticMetricDimension> dimensions;
    private List<ElasticMetricDatapoint> datapoints;

    public ElasticMetricAggregations() {
    }

    public List<ElasticMetricDimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ElasticMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<ElasticMetricDatapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<ElasticMetricDatapoint> datapoints) {
        this.datapoints = datapoints;
    }

    public void addDatapoint(ElasticMetricDatapoint datapoint) {
        if(datapoints == null) {
            datapoints = new ArrayList<>();
        }
        if(datapoints.contains(datapoint) == false) {
            datapoints.add(datapoint);
        }
    }

    public void addDimension(ElasticMetricDimension dimension) {
        if(dimensions == null) {
            dimensions = new ArrayList<>();
        }
        if(dimensions.contains(dimension) == false) {
            dimensions.add(dimension);
        }
    }
}
