package com.spotinst.metrics.commons.converters;

import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.metrics.api.model.request.ApiDimensionsValuesRequest;
import com.spotinst.metrics.api.model.response.ApiDimensionsValuesResponse;
import com.spotinst.metrics.bl.model.request.BlDimensionsValuesRequest;
import com.spotinst.metrics.bl.model.response.BlDimensionsValuesResponse;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticDimensionsValuesRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticDimensionsValuesResponse;

public class DimensionsValuesConverter {

    public static ElasticDimensionsValuesRequest toEs(BlDimensionsValuesRequest request) {
        ElasticDimensionsValuesRequest result =
                EntitiesMapper.instance.mapType(request, ElasticDimensionsValuesRequest.class);
        return result;
    }

    public static BlDimensionsValuesResponse toBl(ElasticDimensionsValuesResponse response) {
        // Skip Orika mapper to avoid creating redundant instances of the result map
        BlDimensionsValuesResponse retVal = new BlDimensionsValuesResponse();
        if (response != null) {
            retVal.setDimensions(response.getDimensions());
        }
        return retVal;
    }

    public static BlDimensionsValuesRequest toBl(ApiDimensionsValuesRequest request) {
        BlDimensionsValuesRequest retVal = EntitiesMapper.instance.mapType(request, BlDimensionsValuesRequest.class);
        return retVal;
    }

    public static ApiDimensionsValuesResponse toApi(BlDimensionsValuesResponse response) {
        // Skip Orika mapper to avoid creating redundant instances of the result map
        ApiDimensionsValuesResponse retVal = new ApiDimensionsValuesResponse();
        if (response != null) {
            retVal.setDimensions(response.getDimensions());
        }
        return retVal;
    }
}
