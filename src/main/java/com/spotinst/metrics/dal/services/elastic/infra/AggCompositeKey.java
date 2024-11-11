package com.spotinst.metrics.dal.services.elastic.infra;

public class AggCompositeKey {
    private String termKey;
    private String timestampKey;

    public AggCompositeKey() {
    }

    public AggCompositeKey(String termKey) {
        this.termKey = termKey;
    }

    public String getTermKey() {
        return termKey;
    }

    public void setTermKey(String termKey) {
        this.termKey = termKey;
    }

    public String getTimestampKey() {
        return timestampKey;
    }

    public void setTimestampKey(String timestampKey) {
        this.timestampKey = timestampKey;
    }
}
