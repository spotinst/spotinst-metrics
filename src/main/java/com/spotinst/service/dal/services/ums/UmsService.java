package com.spotinst.service.dal.services.ums;

import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.dropwizard.common.filters.auth.IAuthTokenActions;
import com.spotinst.dropwizard.common.filters.auth.token.OAuthAccessToken;
import com.spotinst.dropwizard.common.rest.RestClient;
import com.spotinst.dropwizard.common.rest.RestResponse;
import com.spotinst.dropwizard.common.service.BaseSpotinstService;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.commons.configuration.SampleConfiguration;
import com.spotinst.service.dal.models.ums.token.response.ResolveTokenResponse;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public class UmsService extends BaseSpotinstService implements IAuthTokenActions {

    private static final String UMS_RESOLVE_TOKEN_URL = "%s/auth/resolve";
    private static final String UMS_VERIFY_TOKEN_URL  = "%s/auth/verify";

    private static final String UMS_TOKEN_QUERY_PARAM_NAME = "token";

    private static final String UMS_SERVICE_URL;

    static {
        SampleConfiguration configuration = SampleAppContext.getInstance().getConfiguration();
        UMS_SERVICE_URL = configuration.getServices().getUmsServiceUrl();
    }

    public Boolean verifyToken(String token) throws DalException {
        return verify(token);
    }

    public OAuthAccessToken resolveToken(String token) throws DalException {
        return resolve(token);
    }

    public static Boolean verify(String token) throws DalException {
        Boolean retVal = false;

        Map<String, String> headers = buildHeaders(true);

        Map<String, String> queryParams = buildQueryParams();
        queryParams.put(UMS_TOKEN_QUERY_PARAM_NAME, token);

        String url = String.format(UMS_VERIFY_TOKEN_URL, UMS_SERVICE_URL);

        RestResponse response = RestClient.sendGet(url, headers, queryParams);

        if (response.getStatusCode() == HttpStatus.SC_OK) {
            retVal = true;
        }

        return retVal;
    }

    public static OAuthAccessToken resolve(String token) throws DalException {
        OAuthAccessToken retVal = null;

        Map<String, String> headers = buildHeaders(true);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(UMS_TOKEN_QUERY_PARAM_NAME, token);

        String url = String.format(UMS_RESOLVE_TOKEN_URL, UMS_SERVICE_URL);

        RestResponse response = RestClient.sendGet(url, headers, queryParams);

        ResolveTokenResponse castedResponse = getCastedResponse(response, ResolveTokenResponse.class);
        if (castedResponse.getResponse().getCount() > 0) {
            retVal = castedResponse.getResponse().getItems().get(0);
        }

        return retVal;
    }
}
