package com.spotinst.metrics.commons.configuration.ratelimit;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by aharontwizer on 5/9/16.
 */
@Setter
@Getter
public class DbMappingConfiguration {

    private String dbName;
    private String organizationsRange;

    @Override
    public String toString() {
        return "DbMappingConfiguration{" + "dbName='" + dbName + '\'' + ", organizationsRange='" + organizationsRange +
               '\'' + '}';
    }
}
