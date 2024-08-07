package com.spotinst.metrics.commons.converters;

import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.metrics.api.model.ApiMetric;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiMetricDocument;
import com.spotinst.metrics.api.requests.ApiMetricsCreateRequest;
import com.spotinst.metrics.bl.model.BlMetric;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.model.BlMetricDimension;
import com.spotinst.metrics.bl.model.BlMetricDocument;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetricConverter {
    public static BlMetricCreateRequest apiToBl(ApiMetricsCreateRequest apiRequest) {
        BlMetricCreateRequest retVal = new BlMetricCreateRequest();

        List<BlMetricDocument> blMetricDocuments =
                apiRequest.getMetricDocuments().stream().map(MetricConverter::apiToBl).collect(Collectors.toList());
        retVal.setMetricDocuments(blMetricDocuments);

        return retVal;
    }

    public static BlMetricDocument apiToBl(ApiMetricDocument apiMetricDocument) {
        BlMetricDocument retVal = new BlMetricDocument();

        String accountId = RequestsContextManager.getContext().getAccountId();
        retVal.setAccountId(accountId);

        List<BlMetric> blMetrics = convertMetrics(apiMetricDocument.getMetrics());
        retVal.setMetrics(blMetrics);

        List<BlMetricDimension> blDimensions = convertDimensions(apiMetricDocument.getDimensions());
        retVal.setDimensions(blDimensions);

        retVal.setNamespace(apiMetricDocument.getNamespace());
        retVal.setTimestamp(apiMetricDocument.getTimestamp());

        return retVal;
    }

    private static List<BlMetricDimension> convertDimensions(List<ApiMetricDimension> apiMetricDimensions) {
        List<BlMetricDimension> retVal = new ArrayList<>();

        for (ApiMetricDimension apiDimension : apiMetricDimensions) {
            BlMetricDimension blMetricDimension = new BlMetricDimension();
            blMetricDimension.setName(apiDimension.getName());
            blMetricDimension.setValue(apiDimension.getValue());

            retVal.add(blMetricDimension);
        }
        return retVal;
    }

    private static List<BlMetric> convertMetrics(List<ApiMetric> apiMetrics) {
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

    public static ElasticMetricDocument blToDal(BlMetricDocument blMetricDocument) {
        ElasticMetricDocument retVal = new ElasticMetricDocument();

        retVal.setMetrics(blMetricDocument.getMetrics());
        retVal.setDimensions(blMetricDocument.getDimensions());
        retVal.setAccountId(blMetricDocument.getAccountId());
        retVal.setNamespace(blMetricDocument.getNamespace());
        retVal.setTimestamp(blMetricDocument.getTimestamp());

        return retVal;
    }
}
