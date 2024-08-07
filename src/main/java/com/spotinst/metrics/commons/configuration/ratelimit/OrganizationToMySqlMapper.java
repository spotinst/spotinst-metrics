package com.spotinst.metrics.commons.configuration.ratelimit;

import com.spotinst.dropwizard.common.ratelimit.IOrganizationToMySqlMapper;
import com.spotinst.metrics.MetricsAppContext;

/**
 * Created by zachi.nachshon on 17/07/2018.
 */
public class OrganizationToMySqlMapper implements IOrganizationToMySqlMapper {

    @Override
    public String getMySqlName(String organization) {
        DbOrganizationMappingConfiguration dbMapping = MetricsAppContext.getInstance().getConfiguration().getDbMapping();
        String                             retVal    = dbMapping.getDbNameByOrganization(organization);
        return retVal;
    }
}
