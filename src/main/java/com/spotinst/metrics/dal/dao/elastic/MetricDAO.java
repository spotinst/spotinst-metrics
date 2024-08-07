package com.spotinst.metrics.dal.dao.elastic;

import com.spotinst.metrics.MetricsAppContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
}
