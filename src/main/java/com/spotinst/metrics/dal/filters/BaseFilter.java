package com.spotinst.metrics.dal.filters;

import com.spotinst.dropwizard.bl.repo.IEntityFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseFilter implements IEntityFilter {
    //region Members
    private String  sortBy;
    private String  sortOrder;
    private Integer limit;
    private String  isActive;
    private Date    maxCreatedAt;
    private Date    minCreatedAt;
    private Date    maxUpdatedAt;
    private Date    minUpdatedAt;
    //endregion

    //region Getters and Setters
    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public Date getMaxCreatedAt() {
        return maxCreatedAt;
    }

    public void setMaxCreatedAt(Date maxCreatedAt) {
        this.maxCreatedAt = maxCreatedAt;
    }

    public Date getMinCreatedAt() {
        return minCreatedAt;
    }

    public void setMinCreatedAt(Date minCreatedAt) {
        this.minCreatedAt = minCreatedAt;
    }

    public Date getMaxUpdatedAt() {
        return maxUpdatedAt;
    }

    public void setMaxUpdatedAt(Date maxUpdatedAt) {
        this.maxUpdatedAt = maxUpdatedAt;
    }

    public Date getMinUpdatedAt() {
        return minUpdatedAt;
    }

    public void setMinUpdatedAt(Date minUpdatedAt) {
        this.minUpdatedAt = minUpdatedAt;
    }
    //endregion

    //region override methods
    @Override
    public Map<String, String> asQueryParams() {
        Map<String, String> retVal = new HashMap<>();

        if (sortBy != null) {
            retVal.put("sortBy", sortBy);
        }

        if (sortOrder != null) {
            retVal.put("sortOrder", sortOrder);
        }

        if (limit != null) {
            retVal.put("limit", Integer.toString(limit));
        }

        if (isActive != null) {
            retVal.put("isActive", isActive);
        }

        if (maxCreatedAt != null) {
            retVal.put("maxCreatedAt", Long.toString(maxCreatedAt.getTime()));
        }

        if (minCreatedAt != null) {
            retVal.put("minCreatedAt", Long.toString(minCreatedAt.getTime()));
        }

        if (maxUpdatedAt != null) {
            retVal.put("maxUpdatedAt", Long.toString(maxUpdatedAt.getTime()));
        }

        if (minUpdatedAt != null) {
            retVal.put("minUpdatedAt", Long.toString(minUpdatedAt.getTime()));
        }

        return retVal;
    }
    //endregion
}
