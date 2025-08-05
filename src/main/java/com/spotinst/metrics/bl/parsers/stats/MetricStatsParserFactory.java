package com.spotinst.metrics.bl.parsers.stats;

import com.spotinst.metrics.commons.enums.MetricStatisticEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricStatsParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatsParserFactory.class);

    public static IMetricStatParser getParser(MetricStatisticEnum statistic) {
        IMetricStatParser retVal = null;

        switch (statistic) {
            case AVERAGE: {
                retVal = new MetricAverageParser();
                break;
            }
            case SUM: {
                retVal = new MetricSumParser();
                break;
            }
            case MAXIMUM: {
                retVal = new MetricMaxParser();
                break;
            }
            case MINIMUM: {
                retVal = new MetricMinParser();
                break;
            }
            case COUNT: {
                retVal = new MetricCountParser();
                break;
            }
            default: {
                String format = "Cannot determine parser for statistic [%s]";
                String errMsg = String.format(format, statistic.getValue());
                LOGGER.error(errMsg);
                break;
            }
        }

        return retVal;
    }
}
