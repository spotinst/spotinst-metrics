package com.spotinst.metrics.dal.services.db.infra;

import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.commons.response.api.ServiceAffectedRowsResponse;
import com.spotinst.commons.response.dal.base.BaseServiceItemsResponse;
import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.bl.service.GenericService;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.dropwizard.common.rest.RestClient;
import com.spotinst.dropwizard.common.rest.RestResponse;
import org.apache.hc.core5.http.ContentType;

import java.util.List;
import java.util.Map;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public class BulkGenericService<Id, DbEntity, Filter extends IEntityFilter, BulkRequest> extends GenericService<Id, DbEntity, Filter> implements IBulkGenericService<Id, DbEntity, Filter, BulkRequest> {
    //region Members
    private static final String BULK_URL_FORMAT = "%s/bulk";
    //endregion

    //region Constructors

    protected BulkGenericService() {
    }


    public BulkGenericService(String serviceUrl, Class<? extends BaseServiceItemsResponse<DbEntity>> responseClass) {
        this.serviceUrl = serviceUrl;
        this.responseClass = responseClass;
    }

    public BulkGenericService(String serviceUrl, boolean useCoreToken,
                              Class<? extends BaseServiceItemsResponse<DbEntity>> responseClass) {
        super(serviceUrl, useCoreToken, responseClass);
        this.serviceUrl = serviceUrl;
        this.useCoreToken = useCoreToken;
        this.responseClass = responseClass;
    }
    //endregion

    //region Public Methods

    @Override
    public boolean createBulk(BulkRequest request) throws DalException {
        Boolean retVal = false;

        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String body = JsonMapper.toJson(request);

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        RestResponse response = RestClient.sendPost(url, body, headers, queryParams, 0);

        ServiceAffectedRowsResponse casted = getCastedResponse(response, ServiceAffectedRowsResponse.class);
        if (casted.getResponse().getAffectedRows() > 0) {
            retVal = true;
        }

        return retVal;
    }

    @Override
    public boolean updateBulk(BulkRequest request) throws DalException {
        boolean retVal;

        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String body = JsonMapper.toJson(request);

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        RestResponse response = RestClient.sendPut(url, body, headers, queryParams, 0, ContentType.APPLICATION_JSON);

        ServiceAffectedRowsResponse casted = getCastedResponse(response, ServiceAffectedRowsResponse.class);
        retVal = casted.getResponse().getAffectedRows() > 0;

        return retVal;
    }

    @Override
    public boolean deleteBulk(List<Id> ids) throws DalException {
        boolean retVal;

        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        BulkIdentifierRequest<Id> request = new BulkIdentifierRequest<>();
        request.setIds(ids);

        String body = JsonMapper.toJson(request);

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        RestResponse response = RestClient.sendDelete(url, body, headers, queryParams);

        ServiceAffectedRowsResponse casted = getCastedResponse(response, ServiceAffectedRowsResponse.class);
        retVal = casted.getResponse().getAffectedRows() > 0;

        return retVal;
    }

    @Override
    public void asyncCreateBulk(BulkRequest request) {
        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String body = JsonMapper.toJson(request);

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        RestClient.sendAsyncPost(url, body, headers, queryParams);
    }

    @Override
    public void asyncUpdateBulk(BulkRequest request) {
        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String body = JsonMapper.toJson(request);

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        RestClient.sendAsyncPut(url, body, headers, queryParams);
    }

    @Override
    public void asyncDeleteBulk(List<Id> ids) {
        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String url = String.format(BULK_URL_FORMAT, serviceUrl);

        BulkIdentifierRequest<Id> request = new BulkIdentifierRequest<>();
        String                    body    = JsonMapper.toJson(request);

        RestClient.sendAsyncDelete(url, body, headers, queryParams);
    }

    //endregion
}
