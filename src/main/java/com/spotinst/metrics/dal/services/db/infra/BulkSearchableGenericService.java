package com.spotinst.metrics.dal.services.db.infra;

import com.spotinst.commons.mapper.json.JsonMapper;
import com.spotinst.commons.response.dal.base.BaseServiceItemsResponse;
import com.spotinst.dropwizard.bl.repo.IEntityFilter;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.dropwizard.common.rest.RestClient;
import com.spotinst.dropwizard.common.rest.RestResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zachi.nachshon on 7/15/17.
 */
public class BulkSearchableGenericService<Id, DbEntity, Filter extends IEntityFilter, BulkRequest, SearchRequest> extends BulkGenericService<Id, DbEntity, Filter, BulkRequest> implements IBulkSearchableGenericService<Id, DbEntity, Filter, BulkRequest, SearchRequest> {

    //region Members
    private static final String SEARCH_URL_FORMAT = "%s/search";
    //endregion

    //region Constructors
    public BulkSearchableGenericService(String serviceUrl,
                                        Class<? extends BaseServiceItemsResponse<DbEntity>> responseClass) {
        this.serviceUrl = serviceUrl;
        this.responseClass = responseClass;
    }

    public BulkSearchableGenericService(String serviceUrl, boolean useCoreToken,
                                        Class<? extends BaseServiceItemsResponse<DbEntity>> responseClass) {
        super(serviceUrl, useCoreToken, responseClass);
        this.serviceUrl = serviceUrl;
        this.useCoreToken = useCoreToken;
        this.responseClass = responseClass;
    }
    //endregion

    //region Public Methods
    @Override
    public List<DbEntity> search(SearchRequest request) throws DalException {
        List<DbEntity> retVal = new LinkedList<>();

        Map<String, String> headers = useCoreToken ? buildHeaders(true) : buildHeaders();

        Map<String, String> queryParams = buildQueryParams();

        String body = JsonMapper.toJson(request);

        String url = String.format(SEARCH_URL_FORMAT, serviceUrl);

        RestResponse response = RestClient.sendPostNew(url, body, headers, queryParams);

        BaseServiceItemsResponse<DbEntity> casted = getCastedResponse(response, responseClass);
        if (casted.getResponse().getCount() > 0) {
            retVal = casted.getResponse().getItems();
        }

        return retVal;
    }
    //endregion
}
