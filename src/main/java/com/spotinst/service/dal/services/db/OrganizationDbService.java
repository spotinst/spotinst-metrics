package com.spotinst.service.dal.services.db;

import com.spotinst.commons.models.dal.DbOrganization;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.dropwizard.common.rest.RestClient;
import com.spotinst.dropwizard.common.rest.RestResponse;
import com.spotinst.dropwizard.common.service.BaseSpotinstService;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.commons.configuration.ServicesConfig;
import com.spotinst.service.dal.models.core.organization.response.OrganizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OrganizationDbService extends BaseSpotinstService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDbService.class);

    private static final String ORGANIZATION_GET_ALL_URL_FORMAT = "%s/core/organization";
    private static final String ORGANIZATION_GET_URL_FORMAT     = "%s/core/organization/%s";

    private static String SPOTINST_DB_JAVA_SERVICE;

    static {
        ServicesConfig services = SampleAppContext.getInstance().getConfiguration().getServices();
        SPOTINST_DB_JAVA_SERVICE = services.getDbServiceUrl();
    }

    // region Organization
    public static DbOrganization getOrganization(BigInteger organizationId) throws DalException {
        DbOrganization retVal = null;

        Map<String, String> headers = buildHeaders(true);

        Map<String, String> queryParams = new HashMap<>();

        String url = String.format(ORGANIZATION_GET_URL_FORMAT, SPOTINST_DB_JAVA_SERVICE, organizationId);

        RestResponse response = RestClient.sendGet(url, headers, queryParams);

        OrganizationResponse organizationResponse = getCastedResponse(response, OrganizationResponse.class);

        if (organizationResponse.getResponse().getCount() > 0) {
            retVal = organizationResponse.getResponse().getItems().get(0);
        }

        return retVal;
    }

    public static List<DbOrganization> getOrganizations() throws DalException {
        List<DbOrganization> retVal = new LinkedList<>();

        Map<String, String> queryParams = new HashMap<>();

        Map<String, String> headers = buildHeaders(true);

        String url = String.format(ORGANIZATION_GET_ALL_URL_FORMAT, SPOTINST_DB_JAVA_SERVICE);

        RestResponse response = RestClient.sendGet(url, headers, queryParams);

        OrganizationResponse organizationResponse = getCastedResponse(response, OrganizationResponse.class);

        if (organizationResponse.getResponse().getCount() > 0) {
            retVal = organizationResponse.getResponse().getItems();
        }

        return retVal;
    }
    // endregion
}
