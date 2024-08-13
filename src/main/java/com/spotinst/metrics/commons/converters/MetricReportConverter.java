package com.spotinst.metrics.commons.converters;

import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.metrics.api.model.ApiMetric;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiMetricDocument;
import com.spotinst.metrics.api.requests.ApiMetricsCreateRequest;
import com.spotinst.metrics.api.responses.ApiMetricStatisticsResponse;
import com.spotinst.metrics.bl.model.*;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.dal.models.elastic.ElasticMetric;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricReportRequest;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricReportConverter {
    public static BlMetricCreateRequest apiToBl(ApiMetricsCreateRequest apiRequest) {
        BlMetricCreateRequest retVal = new BlMetricCreateRequest();

        List<BlMetricDocument> blMetricDocuments =
                apiRequest.getMetricDocuments().stream().map(MetricReportConverter::apiToBl).collect(Collectors.toList());
        retVal.setMetricDocuments(blMetricDocuments);

        return retVal;
    }

    public static BlMetricDocument apiToBl(ApiMetricDocument apiMetricDocument) {
        BlMetricDocument retVal = new BlMetricDocument();

        String accountId = RequestsContextManager.getContext().getAccountId();
        retVal.setAccountId(accountId);

        List<BlMetric> blMetrics = convertMetricsApiToBl(apiMetricDocument.getMetrics());
        retVal.setMetrics(blMetrics);

        List<BlMetricDimension> blDimensions = convertDimensionsApiToBl(apiMetricDocument.getDimensions());
        retVal.setDimensions(blDimensions);

        retVal.setNamespace(apiMetricDocument.getNamespace());
        retVal.setTimestamp(apiMetricDocument.getTimestamp());

        return retVal;
    }


    private static List<BlMetricDimension> convertDimensionsApiToBl(List<ApiMetricDimension> apiMetricDimensions) {
        List<BlMetricDimension> retVal = new ArrayList<>();

        for (ApiMetricDimension apiDimension : apiMetricDimensions) {
            BlMetricDimension blMetricDimension = new BlMetricDimension();
            blMetricDimension.setName(apiDimension.getName());
            blMetricDimension.setValue(apiDimension.getValue());

            retVal.add(blMetricDimension);
        }
        return retVal;
    }

    private static List<BlMetric> convertMetricsApiToBl(List<ApiMetric> apiMetrics) {
        List<BlMetric> retVal = new ArrayList<>();

        for (ApiMetric apiMetric : apiMetrics) {
            BlMetric blMetric = new BlMetric();
            blMetric.setCount(apiMetric.getCount());
            blMetric.setName(apiMetric.getName());
            blMetric.setValue(apiMetric.getValue());
            blMetric.setUnit(apiMetric.getUnit());

            retVal.add(blMetric);
        }
        return retVal;
    }

    public static ApiMetricStatisticsResponse blToApi(BlMetricStatisticsResponse response) {
        ApiMetricStatisticsResponse retVal = EntitiesMapper.instance.mapType(response, ApiMetricStatisticsResponse.class);
        return retVal;
    }

//    public static ElasticMetricDocument blToDal(BlMetricDocument blMetricDocument) {
//        ElasticMetricDocument retVal = new ElasticMetricDocument();
//
//        retVal.setMetrics(blMetricDocument.getMetrics());
//        retVal.setDimensions(blMetricDocument.getDimensions());
//        retVal.setAccountId(blMetricDocument.getAccountId());
//        retVal.setNamespace(blMetricDocument.getNamespace());
//        retVal.setTimestamp(blMetricDocument.getTimestamp());
//
//        return retVal;
//    }

    public static ElasticMetricReportRequest toEs(BlMetricCreateRequest request) {
        ElasticMetricReportRequest retVal = new ElasticMetricReportRequest();
        List<ElasticMetricDocument> esDocumentList = new ArrayList<>();

        if (request != null && CollectionUtils.isEmpty(request.getMetricDocuments()) == false) {
            
            for (BlMetricDocument blDocument : request.getMetricDocuments()){
                ElasticMetricDocument esDocument = new ElasticMetricDocument();
                
                if (CollectionUtils.isEmpty(blDocument.getMetrics()) == false){
                    Map<String, ElasticMetric> esMetrics = convertMetricsBlToDal(blDocument.getMetrics());
                    esDocument.setMetrics(esMetrics);
                }

                if (CollectionUtils.isEmpty(blDocument.getDimensions()) == false){
                    Map<String, String> esDimensions = convertDimensionsBlToDal(blDocument.getDimensions());
                    esDocument.setDimensions(esDimensions);
                }
                
                if (blDocument.getAccountId() != null){
                    esDocument.setAccountId(blDocument.getAccountId());
                }
                
                if (blDocument.getNamespace() != null){
                    esDocument.setNamespace(blDocument.getNamespace());
                }
                
                if (blDocument.getTimestamp() != null){
                    esDocument.setTimestamp(blDocument.getTimestamp());
                }
                esDocumentList.add(esDocument);
            }
        }
        retVal.setMetricDocuments(esDocumentList);
        return retVal;
    }

    private static Map<String, String> convertDimensionsBlToDal(List<BlMetricDimension> dimensions) {
        Map<String, String> retVal = new HashMap<>();

        for (BlMetricDimension blDimension : dimensions){
            retVal.put(blDimension.getName(), blDimension.getValue());
        }
        return retVal;
    }

    private static Map<String, ElasticMetric> convertMetricsBlToDal(List<BlMetric> metrics) {
        Map<String, ElasticMetric> retVal = new HashMap<>();
        
        for (BlMetric blMetric : metrics){
            ElasticMetric esMetric = new ElasticMetric();
            esMetric.setName(blMetric.getName());
            esMetric.setCount(blMetric.getCount());
            esMetric.setValue(blMetric.getValue());
            esMetric.setUnit(blMetric.getUnit());
            retVal.put(blMetric.getName(), esMetric);
        }
        return retVal;
    }
}
