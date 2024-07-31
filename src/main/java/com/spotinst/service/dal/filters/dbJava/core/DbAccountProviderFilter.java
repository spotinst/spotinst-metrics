package com.spotinst.service.dal.filters.dbJava.core;


import com.spotinst.service.commons.enums.CloudProviderEnum;
import com.spotinst.service.dal.filters.BaseFilter;

/**
 * @author Stas Radchenko
 * @since 11/4/19
 */
public class DbAccountProviderFilter extends BaseFilter {
    //region Members
    private CloudProviderEnum cloudProvider;
    //endregion

    //region Getters & Setters
    public CloudProviderEnum getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(CloudProviderEnum cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
    //endregion

}
