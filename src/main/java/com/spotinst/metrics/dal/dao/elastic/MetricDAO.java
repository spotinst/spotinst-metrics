package com.spotinst.metrics.dal.dao.elastic;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricStatistics;
import com.spotinst.metrics.dal.models.elastic.common.ElasticSearchRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetricDAO extends BaseElasticDAO {
    @Override
    protected String getIndexSuffix() {
        String retVal;

        retVal = getDateSuffix(0);

        return retVal;
    }

    @Override
    protected String getBaseIndexName() {
        return "metrics_report_test";
    }

    public String getDateSuffix(int monthsToReduce) {
        Date     now      = MetricsAppContext.getInstance().getSystemClock().getCurrentTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -monthsToReduce);
        Date             dateToFormat = calendar.getTime();
        SimpleDateFormat dateFormat   = new SimpleDateFormat("MM-yyyy");

        String retVal = dateFormat.format(dateToFormat);

        return retVal;
    }

    public List<ElasticMetricStatistics> getAll(BlMetricStatisticsRequest request, String index) throws IOException {
        List<ElasticMetricStatistics> retVal;
        ElasticSearchRequest esGetMetricStatsRequest = buildSearchRequest(request);
        retVal = search(esGetMetricStatsRequest, ElasticMetricStatistics.class);

        return retVal;
    }

    private ElasticSearchRequest buildSearchRequest(BlMetricStatisticsRequest request) {
        ElasticSearchRequest              retVal  = new ElasticSearchRequest();
        List<ElasticSearchRequest.Filter> filters = new LinkedList<>();
        ElasticSearchRequest.Filter filter = BuildSearchFilter(request);
        filters.add(filter);
        retVal.setFilters(filters);

        return retVal;
    }

    private ElasticSearchRequest.Filter BuildSearchFilter(BlMetricStatisticsRequest request) {
        ElasticSearchRequest.Filter retVal;
        Map<String, Object>          matches = new HashMap<>();

        if (Objects.nonNull(request.getAccountId())) {
            matches.put("accountId", request.getAccountId());
        }

        if (Objects.nonNull(request.getMetricName())) {
            matches.put("metricName", request.getMetricName());
        }

        if (Objects.nonNull(request.getNamespace())) {
            matches.put("namespace", request.getNamespace());
        }

        //TODO Tal: Need to check
//        if (Objects.nonNull(request.getGroupBy())) {
//            matches.put("groupBy", request.getGroupBy());
//        }

        List<ElasticSearchRequest.Range> ranges = new ArrayList<>();

        ElasticSearchRequest.Range timeRange =
                new ElasticSearchRequest.Range("timestamp", request.getDateRange().getFrom(), request.getDateRange()
                                                                                                     .getTo());
        ranges.add(timeRange);
        //Todo Tal: check if can range can be done
//        ElasticSearchRequest.Range range = new ElasticSearchRequest.Range("range", request.getRange().getMinimum(), request.getRange().getMaximum());

//        List<BlMetricDimension> dimensions = request.getDimensions();
//        if (Objects.nonNull(dimensions)) {
//
//        }
        retVal = new ElasticSearchRequest.Filter(matches, ranges);
        return retVal;
    }
}
