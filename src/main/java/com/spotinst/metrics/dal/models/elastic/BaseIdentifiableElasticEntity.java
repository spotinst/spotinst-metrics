package com.spotinst.metrics.dal.models.elastic;

public abstract class BaseIdentifiableElasticEntity {
    //region Members
    private String id;
    //endregion

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //endregion

}
