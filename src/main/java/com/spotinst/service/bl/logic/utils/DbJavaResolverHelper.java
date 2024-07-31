package com.spotinst.service.bl.logic.utils;

import com.spotinst.dropwizard.common.context.RequestContext;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.service.bl.model.common.BlSqlDBJavaGroupMapping;
import com.spotinst.service.commons.configuration.DbServiceGroupConfig;
import com.spotinst.service.commons.configuration.SampleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author Stas Radchenko
 * @since 2019-08-14
 */
public class DbJavaResolverHelper {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(DbJavaResolverHelper.class);
    //endregion

    //region Public Methods
    public static String getDbServiceUrl() {
        // The default is the existing db URL
        SampleConfiguration config = SampleAppContext.getInstance().getConfiguration();
        String              retVal = config.getServices().getDbServiceUrl();

        try {
            RequestContext context = RequestsContextManager.getContext();

            if (context != null) {
                String organizationIdStr = context.getOrganizationId();
                if (organizationIdStr != null) {
                    BigInteger organizationId = new BigInteger(organizationIdStr);

                    Map<String, BlSqlDBJavaGroupMapping> sqlDBJavaGroupMapping =
                            SampleAppContext.getInstance().getSqlDBJavaGroupMapping();
                    Map<BigInteger, BlOrganization> organizations = SampleAppContext.getInstance().getOrganizations();

                    if (organizations != null && sqlDBJavaGroupMapping != null) {
                        BlOrganization blOrganization = organizations.get(organizationId);

                        if (blOrganization != null) {
                            String                  sqlName                 = blOrganization.getSqlName();
                            BlSqlDBJavaGroupMapping blSqlDBJavaGroupMapping = sqlDBJavaGroupMapping.get(sqlName);

                            if (blSqlDBJavaGroupMapping != null) {
                                Map<String, DbServiceGroupConfig> dbJavaGroupLbUrlMapping =
                                        SampleAppContext.getInstance().getDbJavaGroupLbUrlMapping();

                                String dbJavaGroupName = blSqlDBJavaGroupMapping.getDbJavaGroupName();
                                DbServiceGroupConfig dbServiceGroupConfig =
                                        dbJavaGroupLbUrlMapping.get(dbJavaGroupName);

                                if (dbServiceGroupConfig != null) {
                                    retVal = dbServiceGroupConfig.getUrl();
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(String.format("Failed to get db service url from context, error: %s", ex.getMessage()), ex);
        }

        return retVal;
    }
    //endregion

}
