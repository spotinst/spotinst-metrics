package com.spotinst.metrics.commons.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zachi.nachshon on 2/8/17.
 */
public enum MetricStatisticEnum {
    AVERAGE("average", "avg"),
    COUNT("count", "count"),
    MINIMUM("minimum", "min"),
    MAXIMUM("maximum", "max"),
    SUM("sum", "sum");

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatisticEnum.class);

    private String value;
    private String shortenValue;

    MetricStatisticEnum(String value, String shortenValue) {
        this.value = value;
        this.shortenValue = shortenValue;
    }

    private static Map<String, MetricStatisticEnum> valuesMap;

    static {
        valuesMap = new HashMap<>();
        for (MetricStatisticEnum status : MetricStatisticEnum.values()) {
            valuesMap.put(status.value, status);
        }
    }

    public static MetricStatisticEnum getByName(String name) {
        MetricStatisticEnum retVal = null;

        if (valuesMap.containsKey(name)) {
            retVal = valuesMap.get(name);
        }
        else {
            LOGGER.error(
                    String.format("Value [%s] is not part of the statistic values supported by the MetricStatisticEnum",
                                  name));
        }

        return retVal;
    }

    public static MetricStatisticEnum fromName(String name) {
        MetricStatisticEnum retVal = null;

        for (MetricStatisticEnum metricStatisticEnum : MetricStatisticEnum.values()) {
            if (name.equalsIgnoreCase(
                    metricStatisticEnum.getValue())) { //TODO Tal:Maybe need to check shorten value too
                retVal = metricStatisticEnum;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.error(
                    String.format("Tried to report metrics statistics enum for: %s, but we don't support such type",
                                  name));
        }

        return retVal;
    }

    public String getValue() {
        return value;
    }

    public String getShortenValue() {
        return shortenValue;
    }
}
