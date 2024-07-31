package com.spotinst.service.dal.filters.dbJava;


import com.spotinst.service.dal.filters.BaseFilter;

import java.util.Map;

public class BaseDbJavaFilter extends BaseFilter {

    @Override
    public Map<String, String> asQueryParams() {
        Map<String, String> retVal = super.asQueryParams();

        String sortBy    = getSortBy();
        String sortOrder = getSortOrder();

        if (sortBy != null) {
            String sortString = sortBy;

            if (sortOrder != null) {
                sortString = sortString.concat(String.format(":%s", sortOrder));
            }

            retVal.put("sort", sortString);
        }

        return retVal;
    }
}
