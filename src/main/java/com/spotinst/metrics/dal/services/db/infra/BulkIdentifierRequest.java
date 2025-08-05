package com.spotinst.metrics.dal.services.db.infra;

import java.util.List;

public class BulkIdentifierRequest<T> {
    private List<T> ids;

    public List<T> getIds() {
        return ids;
    }

    public void setIds(List<T> ids) {
        this.ids = ids;
    }
}
