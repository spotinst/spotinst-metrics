package com.spotinst.metrics.commons.configuration;

public class IndexNamePatterns {
    private String readPattern;
    private String writePattern;
    private String aggregationReadPattern;

    public IndexNamePatterns() {
    }

    public String getReadPattern() {
        return readPattern;
    }

    public void setReadPattern(String readPattern) {
        this.readPattern = readPattern;
    }

    public String getWritePattern() {
        return writePattern;
    }

    public void setWritePattern(String writePattern) {
        this.writePattern = writePattern;
    }

    public String getAggregationReadPattern() {
        return aggregationReadPattern;
    }

    public void setAggregationReadPattern(String aggregationReadPattern) {
        this.aggregationReadPattern = aggregationReadPattern;
    }
}

