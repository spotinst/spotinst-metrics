package com.spotinst.metrics.commons.utils;

import com.spotinst.commons.utils.clock.ISystemClock;
import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.ElasticConfig;
import com.spotinst.metrics.commons.configuration.IndexNamePatterns;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDateRange;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zachi.nachshon on 3/2/17.
 */
public class EsIndexNamingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsIndexNamingUtils.class);

    // Index name format
    private static final String DATED_INDEX_NAME_FORMAT = "metrics-%s";
//    private static final String DATED_INDEX_NAME_FORMAT = "%s";
    private static final String INDEX_TIME_PATTERN      = "yyyy.MM.dd";

    // Index name patterns for read/write actions
    private static String WRITE_INDEX_NAME_PATTERN;
    private static String READ_INDEX_NAME_PATTERN;
    private static String AGG_READ_INDEX_NAME_PATTERN;

    static {
        ElasticConfig config = MetricsAppContext.getInstance()
                                                .getConfiguration()
                                                .getElastic();
        WRITE_INDEX_NAME_PATTERN = "%s";
        READ_INDEX_NAME_PATTERN = "%s";

        IndexNamePatterns indexNamePatterns = config.getIndexNamePatterns();

        if(indexNamePatterns != null) {
            String writePattern = indexNamePatterns.getWritePattern();

            if(StringUtils.isEmpty(writePattern) == false) {
                WRITE_INDEX_NAME_PATTERN = writePattern;
            }

            String readPattern = indexNamePatterns.getReadPattern();

            if(StringUtils.isEmpty(readPattern) == false) {
                READ_INDEX_NAME_PATTERN = readPattern;
            }

            String aggReadPattern = indexNamePatterns.getAggregationReadPattern();

            if(StringUtils.isEmpty(aggReadPattern) == false) {
                AGG_READ_INDEX_NAME_PATTERN = aggReadPattern;
            }
        }
    }

    public static String generateDailyIndexName(String index) {
        Boolean useWriteIndexNameFormat = true;
        String retVal = generateDailyIndexName(useWriteIndexNameFormat, index);
        return retVal;
    }

    public static String generateDailyIndexName(boolean useWriteIndexPattern, String index) {
        String retVal;

        String useFormat;

        if(index == null) {
            useFormat = useWriteIndexPattern ? WRITE_INDEX_NAME_PATTERN : READ_INDEX_NAME_PATTERN;
        }
        else {
            useFormat = generateOverriddenIndex(index);
        }

        ISystemClock systemClock = MetricsAppContext.getInstance().getSystemClock();
        Date         today       = systemClock.getCurrentTime();

        SimpleDateFormat formatter       = new SimpleDateFormat(INDEX_TIME_PATTERN);
        String           formattedDate   = formatter.format(today);
        String           idxNameWithDate = String.format(DATED_INDEX_NAME_FORMAT, formattedDate);

        retVal = String.format(useFormat, idxNameWithDate);
        return retVal;
    }

    private static String generateOverriddenIndex(String index) {
        String retVal;

        if(index.endsWith("_%s") == false) {
            index = index + "-%s";
        }

        retVal = index;

        return retVal;
    }

    public static String[] generateIndicesByDateRange(ElasticMetricDateRange dateRange, String index) {
        Boolean useAggregationIndex = false;
        return generateIndicesByDateRange(dateRange, index, useAggregationIndex);
    }

    public static String[] generateIndicesByDateRange(ElasticMetricDateRange dateRange, String index, Boolean useAggregationIndex) {
        String[]         retVal;
        SimpleDateFormat formatter = new SimpleDateFormat(INDEX_TIME_PATTERN);
        Date             from      = dateRange.getFrom();
        Date             to        = dateRange.getTo();

        if(dateRange == null) {
            Boolean useWriteIndexNameFormat = false;
            String idxName = generateDailyIndexName(useWriteIndexNameFormat, index);
            retVal = new String[]{idxName};
        } else {
            String readIndexPattern = null;

            if(index == null) {
                readIndexPattern = useAggregationIndex ? AGG_READ_INDEX_NAME_PATTERN : READ_INDEX_NAME_PATTERN;
            }
            else {
                readIndexPattern = generateOverriddenIndex(index);
            }

            // Should fail on null pointer if date range values partially supplied
            LOGGER.debug(String.format("Generating indices by date range [%s] - [%s]", from.toString(), to.toString()));

            Set<String> datesSet = new HashSet<>();

            DateTime fromTime = new DateTime(from);
            DateTime nowTime  = new DateTime(to);

            String currDate;
            String idxName;
            String idxNameWithDate;
            for (DateTime date = fromTime; date.isBeforeNow(); date = date.plusDays(1)) {
                currDate = formatter.format(date.toDate());
                idxNameWithDate = String.format(DATED_INDEX_NAME_FORMAT, currDate);
                idxName = String.format(readIndexPattern, idxNameWithDate);
                datesSet.add(idxName);
            }
            currDate = formatter.format(nowTime.toDate());
            idxNameWithDate = String.format(DATED_INDEX_NAME_FORMAT, currDate);
            idxName = String.format(readIndexPattern, idxNameWithDate);
            datesSet.add(idxName);

            LOGGER.debug(String.format("Successfully generated [%s] indices for date range [%s] - [%s]", datesSet.size(),
                                       from.toString(), to.toString()));

            retVal = datesSet.toArray(new String[datesSet.size()]);
        }

        return retVal;
    }
}