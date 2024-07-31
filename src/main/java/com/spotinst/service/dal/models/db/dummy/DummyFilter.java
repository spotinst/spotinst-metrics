package com.spotinst.service.dal.models.db.dummy;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.service.commons.enums.DummyEnum;

import java.util.HashMap;
import java.util.Map;

public class DummyFilter implements IEntityFilter {
    private String    accountId;
    private String    name;
    private DummyEnum type;

    public DummyFilter(String accountId, String name, DummyEnum type) {
        this.accountId = accountId;
        this.name = name;
        this.type = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DummyEnum getType() {
        return type;
    }

    public void setType(DummyEnum type) {
        this.type = type;
    }

    @Override
    public Map<String, String> asQueryParams() {
        Map<String, String> retVal = new HashMap<>();

        if (this.getAccountId() != null) {
            retVal.put("accountId", this.getAccountId());
        }
        if (this.getName() != null) {
            retVal.put("name", this.getName());
        }
        if (this.getType() != null) {
            retVal.put("type", this.getType().getName());
        }

        return retVal;
    }
}
