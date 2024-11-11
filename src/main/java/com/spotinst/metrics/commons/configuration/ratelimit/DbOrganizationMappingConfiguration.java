package com.spotinst.metrics.commons.configuration.ratelimit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Database.CORE_DB_NAME;
import static com.spotinst.dropwizard.common.constants.ServiceConstants.Database.STATISTICS_DB_NAME;

@Setter
@Getter
public class DbOrganizationMappingConfiguration {

    private String                       coreDb;
    private String                       statisticsDb;
    private List<DbMappingConfiguration> organizationsDb;

    public String getDbName(String organizationId) {
        String retVal;

        if (organizationId.equals(CORE_DB_NAME)) {
            // Organization id was replaced from 'spotinst-core' to 'core' during DbConnectionRequestFilter
            // In this case we're using the 'coreDb' configuration value
            retVal = coreDb;
        }
        else if (organizationId.equals(STATISTICS_DB_NAME)) {
            // Organization id was replaced from 'spotinst-statistics' to 'statistics' during DbConnectionRequestFilter
            // In this case we're using the 'statisticsDb' configuration value
            retVal = statisticsDb;
        }
        else {
            retVal = getDbNameByOrganization(organizationId);
        }

        return retVal;
    }

    public String getDbNameByOrganization(String organizationId) {
        String retVal = null;

        // Organization id is numeric and safe to parse
        long organizationIdLong = Long.parseLong(organizationId);

        for (DbMappingConfiguration dbMappingConfiguration : organizationsDb) {

            // organizationsRange: 606079861806,606079861808-606079861999
            String   organizationsRange = dbMappingConfiguration.getOrganizationsRange();
            String[] expressionArr      = organizationsRange.split(",");

            for (String expression : expressionArr) {

                // Check whether it's a range or single organization id
                String[] rangeExpression = expression.split("-");

                // organizationsRange: 606079861808-606079861999
                if (rangeExpression.length > 1) {

                    // Range
                    String minStr = rangeExpression[0];
                    String maxStr = rangeExpression[1];
                    Long   min    = Long.valueOf(minStr.trim());
                    Long   max    = Long.valueOf(maxStr.trim());

                    if (organizationIdLong >= min && organizationIdLong <= max) {
                        retVal = dbMappingConfiguration.getDbName();
                        break;
                    }
                }
                else {
                    // organizationsRange: 606079861806
                    Long rangeOrganizationId = Long.valueOf(expression.trim());
                    if (organizationIdLong == rangeOrganizationId) {
                        retVal = dbMappingConfiguration.getDbName();
                        break;
                    }
                }
            }

            if (retVal != null) {
                break;
            }
        }

        return retVal;
    }

    @Override
    public String toString() {
        return "DbOrganizationMappingConfiguration{" + "coreDb='" + coreDb + '\'' + ", statisticsDb='" + statisticsDb +
               '\'' + ", organizationsDb=" + organizationsDb + '}';
    }
}
