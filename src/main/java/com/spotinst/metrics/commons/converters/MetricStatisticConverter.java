package com.spotinst.metrics.commons.converters;

import com.spotinst.commons.mapper.entities.EntitiesMapper;
import com.spotinst.metrics.api.model.ApiMetricDateRange;
import com.spotinst.metrics.api.model.ApiMetricDimension;
import com.spotinst.metrics.api.model.ApiMetricRange;
import com.spotinst.metrics.api.requests.ApiMetricStatisticsRequest;
import com.spotinst.metrics.bl.model.*;
import com.spotinst.metrics.bl.model.responses.BlMetricStatisticsResponse;
import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import com.spotinst.metrics.commons.enums.MetricsTimeIntervalEnum;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDatapoint;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDimension;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.requests.ElasticMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.responses.ElasticMetricStatisticsResponse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MetricStatisticConverter {
//    public static BlMetricStatisticsRequest toBl(ApiMetricStatisticsRequest request) {
//        BlMetricStatisticsRequest result =
//                EntitiesMapper.instance.mapType(request, BlMetricStatisticsRequest.class);
//        return result;
//    }

    public static BlMetricStatisticsRequest toBl(ApiMetricStatisticsRequest request) {
        BlMetricStatisticsRequest retVal = new BlMetricStatisticsRequest();

        if (request != null) {
            if (request.getMetricName() != null) {
                retVal.setMetricName(request.getMetricName());
            }

            if (request.getStatistic() != null) {
                retVal.setStatistic(MetricStatisticEnum.fromName(request.getStatistic()));
            }

            if (request.getDateRange() != null) {
                BlMetricDateRange blDateRange = convertDateRange(request.getDateRange());
                retVal.setDateRange(blDateRange);
            }

            if (request.getNamespace() != null){
                retVal.setNamespace(request.getNamespace());
            }

            if (request.getDimensions() != null){
                List<BlMetricDimension> blMetricDimensions = convertDimensions(request.getDimensions());
                retVal.setDimensions(blMetricDimensions);
            }

            if (request.getGroupBy() != null){
                retVal.setGroupBy(request.getGroupBy());
            }

            if (request.getTimeInterval() != null){
                retVal.setTimeInterval(MetricsTimeIntervalEnum.fromName(request.getTimeInterval()));
            }

            if (request.getRange() != null){
                BlMetricRange blMetricRange = convertRange(request.getRange());
                retVal.setRange(blMetricRange);
            }
        }
        return retVal;
    }

    private static BlMetricDateRange convertDateRange(ApiMetricDateRange dateRange) {
        BlMetricDateRange retVal = new BlMetricDateRange();

        if (dateRange.getFrom() != null){
            retVal.setFrom(dateRange.getFrom());
        }

        if (dateRange.getTo() != null){
            retVal.setTo(dateRange.getTo());
        }

        return retVal;
    }

    private static BlMetricRange convertRange(ApiMetricRange range) {
        BlMetricRange retVal = new BlMetricRange();

        if (range.getGap() != null){
            retVal.setGap(range.getGap());
        }

        if (range.getMaximum() != null){
            retVal.setGap(range.getMaximum());
        }

        if (range.getMinimum() != null){
            retVal.setGap(range.getMinimum());
        }

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

    public static BlMetricStatisticsResponse toBl(ElasticMetricStatisticsResponse response) {
        BlMetricStatisticsResponse retVal = new BlMetricStatisticsResponse();

        if(response != null &&
           response.getAggregations() != null &&
           response.getAggregations().isEmpty() == false) {

            List<BlMetricAggregations> blAggs = new LinkedList<>();

            response.getAggregations().forEach(esAgg -> {

                List<ElasticMetricDatapoint> esDps = esAgg.getDatapoints();
                if(esDps != null && esDps.isEmpty() == false) {
                    List<BlMetricDatapoint> blDps = esDps.stream()
                                                         .filter(dp -> dp.getStatistics() != null)
                                                         .map(esDp -> EntitiesMapper.instance.mapType(esDp, BlMetricDatapoint.class))
                                                         .collect(Collectors.toCollection(LinkedList::new));

                    List<BlMetricDimension>      blDims = null;
                    List<ElasticMetricDimension> esDims = esAgg.getDimensions();
                    if(esDims != null && esDims.isEmpty() == false) {
                        blDims = esDims.stream()
                                       .map(esDim -> EntitiesMapper.instance.mapType(esDim, BlMetricDimension.class))
                                       .collect(Collectors.toCollection(LinkedList::new));
                    }

                    BlMetricAggregations blAgg = new BlMetricAggregations();
                    blAgg.setDatapoints(blDps);
                    blAgg.setDimensions(blDims);

                    blAggs.add(blAgg);
                }
            });

            retVal.setAggregations(blAggs);
        }

        return retVal;
    }


    public static ElasticMetricStatisticsRequest toEs(BlMetricStatisticsRequest request) {
        ElasticMetricStatisticsRequest result =
                EntitiesMapper.instance.mapType(request, ElasticMetricStatisticsRequest.class);
        return result;
    }

    public static List<BlMetricStatistics> dalToBl(List<ElasticMetricStatistics> dalMetrics){
        return EntitiesMapper.instance.mapAsList(dalMetrics, BlMetricStatistics.class);
    }
}
