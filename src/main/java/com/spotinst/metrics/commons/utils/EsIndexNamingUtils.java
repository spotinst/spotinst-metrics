package com.spotinst.metrics.commons.utils;

import com.spotinst.commons.utils.clock.ISystemClock;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class EsIndexNamingUtils {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(EsIndexNamingUtils.class);

    private static final String DEFAULT_METRICS_INDEX_NAME_FORMAT = "opt_metrics-%s";
    private static final String INDEX_TIME_PATTERN                = "yyyy.MM.dd";
    //endregion

    //region Public Methods
    // Index name patterns for read/write actions
    public static String generateDailyIndexName(String index) {
        String retVal;

        String indexFormat =
                Objects.nonNull(index) ? generateOverriddenIndex(index) : DEFAULT_METRICS_INDEX_NAME_FORMAT;

        ISystemClock systemClock = MetricsAppContext.getInstance().getSystemClock();
        Date         today       = systemClock.getCurrentTime();

        SimpleDateFormat formatter     = new SimpleDateFormat(INDEX_TIME_PATTERN);
        String           formattedDate = formatter.format(today);
        retVal = String.format(indexFormat, formattedDate);

        return retVal;
    }
    //endregion

    //region Private Methods
    private static String generateOverriddenIndex(String index) {
        String retVal;

        if (BooleanUtils.isFalse(index.endsWith("-%s"))) {
            index = index + "-%s";
        }

        retVal = index;

        return retVal;
    }

    public static List<String> generateIndicesByDateRange(ElasticMetricDateRange dateRange, String index) {
        List<String>     retVal;
        SimpleDateFormat formatter = new SimpleDateFormat(INDEX_TIME_PATTERN);
        Date             from      = dateRange.getFrom();
        Date             to        = dateRange.getTo();

        String indexFormat =
                Objects.nonNull(index) ? generateOverriddenIndex(index) : DEFAULT_METRICS_INDEX_NAME_FORMAT;

        LOGGER.debug(String.format("Generating indices by date range [%s] - [%s]", from.toString(), to.toString()));

        Set<String> datesSet = new HashSet<>();

        DateTime fromTime = new DateTime(from);
        DateTime nowTime  = new DateTime(to);

        String currDate;
        String idxName;

        for (DateTime date = fromTime; date.isBeforeNow(); date = date.plusDays(1)) {
            currDate = formatter.format(date.toDate());
            idxName = String.format(indexFormat, currDate);
            datesSet.add(idxName);
        }

        currDate = formatter.format(nowTime.toDate());
        idxName = String.format(indexFormat, currDate);
        datesSet.add(idxName);

        LOGGER.debug(
                String.format("Successfully generated [%s] indices for date range [%s] - [%s]", datesSet.size(), from,
                              to));

        retVal = new ArrayList<>(datesSet);

        return retVal;
    }
    //endregion
}