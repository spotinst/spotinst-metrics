//package com.spotinst.metrics.commons.converters;
//
//import com.spotinst.commons.mapper.entities.EntitiesMapper;
//import com.spotinst.metrics.api.requests.ApiPercentilesRequest;
//import com.spotinst.metrics.api.responses.ApiPercentilesResponse;
//import com.spotinst.metrics.bl.model.BlPercentileAggregationResult;
//import com.spotinst.metrics.bl.model.BlPercentilesRequest;
//import com.spotinst.metrics.bl.model.responses.BlPercentilesResponse;
//import com.spotinst.metrics.dal.models.elastic.ElasticPercentileAggregationResult;
//import com.spotinst.metrics.dal.models.elastic.requests.ElasticPercentilesRequest;
//import com.spotinst.metrics.dal.models.elastic.responses.ElasticPercentilesResponse;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class PercentilesConverter {
//    public static BlPercentilesRequest toBl(ApiPercentilesRequest request) {
//        BlPercentilesRequest result = EntitiesMapper.instance.mapType(request, BlPercentilesRequest.class);
//        return result;
//    }
//
//    public static BlPercentilesResponse toBl(ElasticPercentilesResponse response) {
//        BlPercentilesResponse retVal = new BlPercentilesResponse();
//
//        if (response != null) {
//            if (response.getTotalHits() != null) {
//                long    totalHist        = response.getTotalHits().value;
//                Integer totalHitsInteger = Math.toIntExact(totalHist);
//                retVal.setTotalHits(totalHitsInteger);
//            }
//
//            if (response.getPercentiles() != null) {
//                List<BlPercentileAggregationResult> blPercentileAggregationResults =
//                        convertPercentileAggregationResults(response.getPercentiles());
//                retVal.setPercentiles(blPercentileAggregationResults);
//            }
//
//            if (response.getLastReportDate() != null) {
//                retVal.setLastReportDate(response.getLastReportDate());
//            }
//        }
//        return retVal;
//    }
//
//    private static List<BlPercentileAggregationResult> convertPercentileAggregationResults(
//            List<ElasticPercentileAggregationResult> percentiles) {
//        List<BlPercentileAggregationResult> retVal = new ArrayList<>();
//
//        for (ElasticPercentileAggregationResult percentile : percentiles) {
//            BlPercentileAggregationResult blPercentileAggregationResult = new BlPercentileAggregationResult();
//            blPercentileAggregationResult.setPercentile(percentile.getPercentile());
//            blPercentileAggregationResult.setCount(percentile.getCount());
//            blPercentileAggregationResult.setName(percentile.getName());
//
//            retVal.add(blPercentileAggregationResult);
//        }
//        return retVal;
//    }
//
//    public static ElasticPercentilesRequest toEs(BlPercentilesRequest request) {
//        ElasticPercentilesRequest result = EntitiesMapper.instance.mapType(request, ElasticPercentilesRequest.class);
//        return result;
//    }
//
//    public static ApiPercentilesResponse toApi(BlPercentilesResponse response) {
//        ApiPercentilesResponse result = EntitiesMapper.instance.mapType(response, ApiPercentilesResponse.class);
//        return result;
//    }
//}
