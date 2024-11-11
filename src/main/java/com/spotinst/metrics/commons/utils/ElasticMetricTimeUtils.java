package com.spotinst.metrics.commons.utils;

import com.spotinst.commons.utils.clock.ISystemClock;
import com.spotinst.metrics.MetricsAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ElasticMetricTimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticMetricTimeUtils.class);

    private static final String TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Formatter with specific time pattern to convert dates with
     *
     * @param date date to format
     * @return formatted date
     */
    public static String dateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        String           formatted = formatter.format(date);
        return formatted;
    }

    public static Long getDiffByTimeUnitFromNow(Date older, TimeUnit unit) {
        Long retVal = getDiffByTimeUnit(older, getNow(), unit);
        return retVal;
    }

    public static Long getDiffByTimeUnit(Date older, Date newer, TimeUnit unit) {
        Long retVal;

        switch(unit) {
            case MINUTES: {
                Long differenceInMinutes = TimeUnit.MINUTES.toMinutes(newer.getTime() - older.getTime());
                retVal = TimeUnit.MILLISECONDS.toMinutes(differenceInMinutes);
                break;
            }
            case HOURS: {
                Long differenceInHours = TimeUnit.HOURS.toHours(newer.getTime() - older.getTime());
                retVal = TimeUnit.MILLISECONDS.toHours(differenceInHours);
                break;
            }
            case DAYS: {
                Long differenceInHours = TimeUnit.DAYS.toDays(newer.getTime() - older.getTime());
                retVal = TimeUnit.MILLISECONDS.toDays(differenceInHours);
                break;
            }
            default: {
                retVal = null;
            }
        }

        return retVal;
    }

    /**
     * Create an ES time unit with 'from' and 'to' fields by using the time unit and offset
     *
     * @param unit time unit
     * @param offset offset from 'now'
     * @return ES time unit
     */
    public static ElasticTimeUnit createOffsetTimeUnitFromNow(TimeUnit unit, Integer offset) {
        ISystemClock systemClock = MetricsAppContext.getInstance().getSystemClock();
        Date         now         = systemClock.getCurrentTime();
        return createOffsetTimeUnit(now, unit, offset);
    }

    /**
     * Create an ES time unit with 'from' and 'to' fields by using the time unit and offset
     *
     * @param endDate the starting date to calculate the offset by
     * @param unit time unit
     * @param offset offset from starting time
     * @return time unit
     */
    public static ElasticTimeUnit createOffsetTimeUnit(Date endDate, TimeUnit unit, Integer offset) {
        ElasticTimeUnit retVal     = null;
        Boolean    foundValue = true;

        Calendar instance = Calendar.getInstance();
        instance.setTime(endDate);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        Date endDateRounded = instance.getTime();

        switch (unit) {
            case MINUTES: {
                instance.add(Calendar.MINUTE, -offset);
                break;
            }
            case HOURS: {
                instance.add(Calendar.HOUR, -offset);
                break;
            }
            case DAYS: {
                instance.add(Calendar.DAY_OF_MONTH, -offset);
                break;
            }
            default: {
                retVal = null;
                foundValue = false;
            }
        }

        if(foundValue) {
            Date   startDateRounded = instance.getTime();
            String nowFormatted     = dateToString(endDateRounded);
            String fromFormatted    = dateToString(startDateRounded);
            retVal = new ElasticTimeUnit(fromFormatted, nowFormatted, startDateRounded, endDateRounded);
        }

        return retVal;
    }

    public static Date createOffsetDate(Date date, TimeUnit unit, Integer offset) {
        Date     retVal     = null;
        Boolean  foundValue = true;
        Calendar cal        = Calendar.getInstance();

        cal.setTime(date);

        switch (unit) {
            case MINUTES: {
                cal.add(Calendar.MINUTE, -offset);
                break;
            }
            case HOURS: {
                cal.add(Calendar.HOUR, -offset);
                break;
            }
            case DAYS: {
                cal.add(Calendar.DAY_OF_MONTH, -offset);
                break;
            }
            default: {
                retVal = null;
                foundValue = false;
            }
        }

        if(foundValue) {
            retVal = cal.getTime();
        }

        return retVal;
    }

    public static int getMinuteOfDate(Date date) {
        int minuteOfHour = 0;

        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            minuteOfHour = calendar.get(Calendar.MINUTE);
        }

        return minuteOfHour;
    }

    public static Date roundSeconds(Date date) {
        Date retVal = null;

        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            retVal = calendar.getTime();
        }

        return retVal;
    }

    public static Date getNow() {
        ISystemClock systemClock = MetricsAppContext.getInstance().getSystemClock();
        Date         now         = systemClock.getCurrentTime();
        return now;
    }
}
