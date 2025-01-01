package com.spotinst.metrics.dal.filters.dbJava.core;


import com.spotinst.commons.enums.ProviderTypeEnum;
import com.spotinst.metrics.dal.filters.BaseFilter;

/**
 * @author Stas Radchenko
 * @since 11/4/19
 */

public class DbAccountProviderFilter extends BaseFilter {
    //region Members
    private ProviderTypeEnum cloudProvider;
    //endregion

    //region Getters & Setters
    public ProviderTypeEnum getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(ProviderTypeEnum cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
    //endregion

}
