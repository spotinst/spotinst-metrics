package com.spotinst.metrics.commons.enums;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Getter
public enum MetricsTimeIntervalEnum {
    ONE_MINUTE("1m", 1, MINUTES),
    TWO_MINUTES("2m", 2, MINUTES),
    FIVE_MINUTES("5m", 5, MINUTES),
    FIFTEEN_MINUTES("15m", 15, MINUTES),
    THIRTY_MINUTES("30m", 30, MINUTES),
    ONE_HOUR("1h", 1, HOURS),
    TWO_HOURS("2h", 2, HOURS),
    SIX_HOURS("6h", 6, HOURS),
    TWELVE_HOURS("12h", 12, HOURS),
    TWENTY_FOUR_HOURS("24h", 24, HOURS);

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsTimeIntervalEnum.class);

    private String   name;
    private Integer  offset;
    private TimeUnit timeUnit;

    private static Map<String, MetricsTimeIntervalEnum> timeIntervals;

    static {
        timeIntervals = new HashMap<>();
        for (MetricsTimeIntervalEnum timeInterval : MetricsTimeIntervalEnum.values()) {
            timeIntervals.put(timeInterval.getName(), timeInterval);
        }
    }

    MetricsTimeIntervalEnum(String name, int offset, TimeUnit timeUnit) {
        this.name = name;
        this.offset = offset;
        this.timeUnit = timeUnit;
    }

    /**
     * Get time interval enum value by name
     *
     * @param name name of the enum value to get
     * @return time interval enum value
     */
    public static MetricsTimeIntervalEnum getByName(String name) {
        MetricsTimeIntervalEnum retVal = null;

        if (timeIntervals.containsKey(name)) {
            retVal = timeIntervals.get(name);
        }
        else {
            LOGGER.error(String.format("Cannot find time interval under the name [%s]", name));
        }

        return retVal;
    }

    public static MetricsTimeIntervalEnum fromName(String name) {
        MetricsTimeIntervalEnum retVal = null;

        for (MetricsTimeIntervalEnum metricsTimeIntervalEnum : MetricsTimeIntervalEnum.values()) {
            if (name.equalsIgnoreCase(metricsTimeIntervalEnum.getName())) {
                retVal = metricsTimeIntervalEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.error(
                    String.format("Tried to create time interval enum for: %s, but we don't support such type", name));
        }

        return retVal;
    }
}
