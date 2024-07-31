package com.spotinst.service.dal.services.db.infra;

import java.util.List;

public class BulkRequest<T> {
    protected List<T> entities;

    public BulkRequest(List<T> entities) {
        this.entities = entities;
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }
}
