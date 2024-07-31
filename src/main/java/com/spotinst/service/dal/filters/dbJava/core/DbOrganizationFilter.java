package com.spotinst.service.dal.filters.dbJava.core;


import com.spotinst.service.dal.filters.dbJava.BaseDbJavaFilter;

import java.util.Map;

public class DbOrganizationFilter extends BaseDbJavaFilter {
    //region Members
    //endregion

    //region Constructors

    public DbOrganizationFilter() {
    }

    //endregion

    //region Getters and Setters
    //endregion

    //region Overridden Methods
    @Override
    public Map<String, String> asQueryParams() {
        Map<String, String> retVal = super.asQueryParams();
        return retVal;
    }
    //endregion

}
