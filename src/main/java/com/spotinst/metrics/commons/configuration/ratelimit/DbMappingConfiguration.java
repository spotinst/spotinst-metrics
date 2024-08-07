package com.spotinst.metrics.commons.configuration.ratelimit;

/**
 * Created by aharontwizer on 5/9/16.
 */
public class DbMappingConfiguration {

    private String dbName;
    private String organizationsRange;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getOrganizationsRange() {
        return organizationsRange;
    }

    public void setOrganizationsRange(String organizationsRange) {
        this.organizationsRange = organizationsRange;
    }

    @Override
    public String toString() {
        return "DbMappingConfiguration{" + "dbName='" + dbName + '\'' + ", organizationsRange='" + organizationsRange +
               '\'' + '}';
    }
}
