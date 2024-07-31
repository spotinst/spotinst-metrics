package com.spotinst.service.commons.configuration.ratelimit;

import com.spotinst.dropwizard.common.ratelimit.IOrganizationToMySqlMapper;
import com.spotinst.service.SampleAppContext;

/**
 * Created by zachi.nachshon on 17/07/2018.
 */
public class OrganizationToMySqlMapper implements IOrganizationToMySqlMapper {

    @Override
    public String getMySqlName(String organization) {
        DbOrganizationMappingConfiguration dbMapping = SampleAppContext.getInstance().getConfiguration().getDbMapping();
        String                             retVal    = dbMapping.getDbNameByOrganization(organization);
        return retVal;
    }
}
