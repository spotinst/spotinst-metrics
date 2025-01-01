package com.spotinst.metrics.commons.converters;

/**
 * Created by zachi.nachshon on 4/2/17.
 */
public class Converters {
    public static EnumConverter             Enum             = new EnumConverter();
    public static CommonConverter           Common           = new CommonConverter();
    public static MetricReportConverter     MetricReport     = new MetricReportConverter();
    public static MetricStatisticConverter  MetricStatistic  = new MetricStatisticConverter();
    public static DimensionsValuesConverter DimensionsValues = new DimensionsValuesConverter();
    public static PercentilesConverter      Percentiles      = new PercentilesConverter();
}
